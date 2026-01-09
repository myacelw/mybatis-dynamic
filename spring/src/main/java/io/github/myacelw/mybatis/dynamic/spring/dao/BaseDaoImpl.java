package io.github.myacelw.mybatis.dynamic.spring.dao;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.ModelService;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * 基本的DAO实现类
 *
 * @author liuwei
 */
@FieldNameConstants
public class BaseDaoImpl<ID, T> implements BaseDao<ID, T>, InitializingBean {
    @Getter
    protected final Class<T> entityClass;

    protected ModelService modelService;

    protected SqlSessionTemplate sqlSessionTemplate;

    /**
     * 是否更新模型对应的数据库表结构
     */
    protected boolean updateModel;

    protected BaseDaoImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public BaseDaoImpl(Class<T> entityClass, ModelService modelService, SqlSessionTemplate sqlSessionTemplate, Boolean updateMode) {
        this(entityClass);
        this.modelService = modelService;
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.updateModel = updateMode == Boolean.TRUE;
    }

    @Override
    public void afterPropertiesSet() {
        register(entityClass);
    }

    @Override
    public DataManager<ID> getDataManager() {
        return modelService.getDataManager(entityClass, sqlSessionTemplate);
    }

    protected void register(@NonNull Class<?> entityClass) {
        Model model = modelService.getModelForClass(entityClass);
        if (updateModel && model.getTableDefine().getDisableTableCreateAndAlter() != Boolean.TRUE) {
            updateModel(model);
        }
        modelService.register(entityClass, model);
    }

    /**
     * 更新模型数据库结构
     */
    protected synchronized void updateModel(Model model) {
        try {
            modelService.update(model, doGetFieldWhiteList(model));
        } catch (Exception e) {
            //异常时重试一次，避免多实例启动时出现并发修改异常
            modelService.update(model, doGetFieldWhiteList(model));
        }

    }

    /**
     * fieldWhiteList 在此名单中的字段才涉及变更，空时不限制。
     */
    protected List<String> doGetFieldWhiteList(Model model) {
        return null;
    }

}

