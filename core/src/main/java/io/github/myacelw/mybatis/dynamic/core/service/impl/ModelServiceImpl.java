package io.github.myacelw.mybatis.dynamic.core.service.impl;

import io.github.myacelw.mybatis.dynamic.core.database.MybatisHelper;
import io.github.myacelw.mybatis.dynamic.core.database.TableManager;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.event.EventListener;
import io.github.myacelw.mybatis.dynamic.core.event.model.DeleteModelEvent;
import io.github.myacelw.mybatis.dynamic.core.event.model.UpdateModelEvent;
import io.github.myacelw.mybatis.dynamic.core.exception.model.ModelException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.Permission;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.GroupField;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.service.*;
import io.github.myacelw.mybatis.dynamic.core.service.*;
import io.github.myacelw.mybatis.dynamic.core.service.filler.Filler;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 模型服务实现类
 *
 * @author liuwei
 */
@Slf4j
public class ModelServiceImpl implements ModelService {

    protected final DataBaseDialect dialect;

    protected final MybatisHelper mybatisHelper;

    protected final TableManager tableManager;

    protected final ModelToTableConverter modelToTableConverter;

    protected final Map<String, Filler> fillers;

    protected final Class2ModelTransfer class2ModelTransfer;

    @Getter
    @Setter
    private List<DataChangeInterceptor> interceptors;

    @Getter
    @Setter
    private PermissionGetter permissionGetter;

    @Getter
    @Setter
    private List<EventListener> eventListeners;

    /**
     * 是否禁用表注释变更
     */
    @Getter
    @Setter
    protected Boolean disableAlterComment;

    protected final Map<String, DataManager<Object>> dataManagerMap = new ConcurrentHashMap<>();

    protected final Map<Class<?>, String> modelNameMap = new ConcurrentHashMap<>();

    public ModelServiceImpl(DataBaseDialect dialect, MybatisHelper mybatisHelper, TableManager tableManager, ModelToTableConverter modelToTableConverter, Map<String, Filler> fillers, Class2ModelTransfer class2ModelTransfer) {
        this.dialect = dialect;
        this.mybatisHelper = mybatisHelper;
        this.tableManager = tableManager;
        this.modelToTableConverter = modelToTableConverter;
        this.fillers = fillers;
        this.class2ModelTransfer = class2ModelTransfer;
    }

    @Override
    public void update(@NonNull Model model, List<String> fieldWhiteList) {
        if (model.getTableDefine().getDisableTableCreateAndAlter() == Boolean.TRUE) {
            log.error("model '{}' disable updated", model.getName());
            throw new ModelException("模型[" + model.getName() + "]禁用了更新操作");
        }

        model.init(modelToTableConverter);
        Table table = modelToTableConverter.convertToTable(model, fieldWhiteList);
        tableManager.createOrUpgradeTable(table);
        EventListener.onEvent(eventListeners, new UpdateModelEvent(model, table));
    }

    /**
     * 删除模型存储数据涉及的数据库表，主要用于测试清空环境使用。
     */
    @Override
    public void delete(@NonNull Model model) {
        if (model.getTableDefine().getDisableTableCreateAndAlter() == Boolean.TRUE) {
            log.error("model '{}' disable deleted", model.getName());
            throw new ModelException("模型[" + model.getName() + "]禁用了删除操作");
        }

        model = model.clone();
        model.addStringIdFieldIfNotExist();
        model.init(modelToTableConverter);
        Table table = modelToTableConverter.convertToTable(model, null);
        this.unregister(model.getName());

        tableManager.dropTable(table);
        EventListener.onEvent(eventListeners, new DeleteModelEvent(model));
    }

    @Override
    public void delete(@NonNull Class<?> entityClass) {
        Model model = getModelForClass(entityClass);
        delete(model);
    }

    @Override
    public Model getModelForClass(Class<?> entityClass) {
        Model model = class2ModelTransfer.getModelForClass(entityClass);

        if (disableAlterComment == Boolean.TRUE) {
            model.getTableDefine().setDisableAlterComment(true);
            for (Field field : model.getFields()) {
                if (field instanceof BasicField) {
                    ((BasicField) field).getColumnDefine().setDisableAlterComment(true);
                } else if (field instanceof GroupField) {
                    for (BasicField basicField : ((GroupField) field).getFields()) {
                        basicField.getColumnDefine().setDisableAlterComment(true);
                    }
                }
            }
        }
        return model;
    }

    @Override
    public void register(@NonNull Model model) {
        DataManager<Object> dataManager = createDataManager(model, null, null);
        dataManagerMap.put(model.getName(), dataManager);
    }

    @Override
    public void register(@NonNull Class<?> entityClass, Model model) {
        if (model == null) {
            model = getModelForClass(entityClass);
        }
        register(model);
        modelNameMap.put(entityClass, model.getName());
    }

    @Override
    public Model unregister(@NonNull String modelName) {
        DataManager<?> dataManager = dataManagerMap.remove(modelName);

        List<Class<?>> classList = new ArrayList<>();
        modelNameMap.forEach((clazz, name) -> {
            if (Objects.equals(name, modelName)) {
                classList.add(clazz);
            }
        });

        if (!classList.isEmpty()) {
            classList.forEach(modelNameMap::remove);
        }

        if (dataManager != null) {
            return dataManager.getModel();
        }
        return null;
    }

    @Override
    public Model updateAndRegister(@NonNull Model model) {
        update(model);
        register(model);
        return model;
    }

    @Override
    public Model updateAndRegister(@NonNull Class<?> entityClass, Model model) {
        if (model != null) {
            update(model);
        } else {
            model = update(entityClass);
        }

        register(entityClass, model);
        return model;
    }

    @Override
    public List<Model> getAllRegisteredModels() {
        return dataManagerMap.values().stream().map(DataManager::getModel).collect(Collectors.toList());
    }

    @Override
    public <ID> DataManager<ID> createDataManager(@NonNull Model model, Permission permission, SqlSession sqlSession) {
        model.init(modelToTableConverter);
        ModelContext modelContext = new ModelContext(model, dialect, mybatisHelper, this, fillers, permission, eventListeners, interceptors, sqlSession);
        return new DataManagerImpl<>(modelContext);
    }

    @Override
    public <ID> DataManager<ID> getDataManager(@NonNull Class<?> entityClass, SqlSession sqlSession) {
        String modelName = modelNameMap.get(entityClass);
        if (modelName == null) {
            throw new IllegalArgumentException("Model Class<" + entityClass.getName() + "> not register");
        }
        return getDataManager(modelName, sqlSession);
    }

    @Override
    public <ID> DataManager<ID> getDataManager(@NonNull String modelName, SqlSession sqlSession) {
        DataManager<ID> dataManager = (DataManager) dataManagerMap.get(modelName);
        if (dataManager == null) {
            throw new IllegalArgumentException("Model<" + modelName + "> not register");
        }

        // 根据当前用户得到带有权限限制的数据管理器
        Permission permission = getPermission(dataManager.getModel());
        if (permission == null) {
            return dataManager;
        }

        return new DataManagerImpl<>(dataManager.getModelContext().createNew(permission, sqlSession));
    }

    @Override
    public boolean isModelExist(String modelName) {
        DataManager<?> dataManager = dataManagerMap.get(modelName);
        return dataManager != null;
    }

    /**
     * 获取模型的权限，子类可以实现这部分
     */
    protected Permission getPermission(Model model) {
        if (permissionGetter != null) {
            return permissionGetter.getPermission(model);
        }
        return null;
    }

}
