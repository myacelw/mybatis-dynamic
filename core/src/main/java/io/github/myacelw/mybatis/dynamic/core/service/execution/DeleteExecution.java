package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.event.data.LogicDeleteDataEvent;
import io.github.myacelw.mybatis.dynamic.core.event.data.PhysicalDeleteDataEvent;
import io.github.myacelw.mybatis.dynamic.core.exception.crud.ConditionParameterException;
import io.github.myacelw.mybatis.dynamic.core.exception.crud.UnsupportedCommandException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.Permission;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.DeleteCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.IdUtil;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import io.github.myacelw.mybatis.dynamic.core.util.tuple.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 删除数据执行器
 *
 * @author liuwei
 */
@Slf4j
public class DeleteExecution<ID> extends AbstractExecution<ID, Integer, DeleteCommand<ID>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return DeleteCommand.class;
    }

    @Override
    public Integer exec(DeleteCommand<ID> command, DataManager<ID> dataManager) {
        ModelContext modelContext = dataManager.getModelContext();

        if (ObjectUtil.isEmpty(command.getIds())) {
            return null;
        }

        boolean physicalDelete = !modelContext.getFieldMap().containsKey(Model.FIELD_DELETE_FLAG);

        if (command.isBatch()) {
            if (command.isForcePhysicalDelete() || physicalDelete) {
                return batchPhysicalDelete(dataManager, command.getIds());
            } else {
                return batchLogicDelete(dataManager, command.getIds());
            }
        } else {
            for (ID id : command.getIds()) {
                if (command.isForcePhysicalDelete() || physicalDelete) {
                    return physicalDelete(dataManager, id);
                } else {
                    return logicDelete(dataManager, id);
                }
            }
        }

        return null;
    }

    private int physicalDelete(DataManager<ID> dataManager, @NonNull ID id) {
        ModelContext modelContext = dataManager.getModelContext();
        Model model = modelContext.getModel();
        Permission permission = modelContext.getPermission();

        //这里忽略了逻辑删除条件
        Condition condition = GroupCondition.and(permission != null ? permission.getDataRights() : null, IdUtil.getIdCondition(model, id));

        modelContext.getInterceptor().beforePhysicalDelete((DataManager) dataManager, id);

        int n = deleteByCondition(modelContext, condition);

        modelContext.getInterceptor().afterPhysicalDelete((DataManager) dataManager, id);
        modelContext.sendEvent(new PhysicalDeleteDataEvent<>(model, id));
        return n;
    }

    public static int deleteByCondition(ModelContext modelContext, @NonNull Condition condition) {
        String whereSql = condition.sql("c", t -> convertColumnForAllField(modelContext, t), modelContext.getDialect());
        if (!StringUtil.hasText(whereSql)) {
            throw new ConditionParameterException("按条件删除数据，Where语句不能为空");
        }
        Map<String, Object> root = new HashMap<>();
        root.put("c", condition);
        root.put("table", modelContext.getModel().getSchemaAndTableName());
        root.put("where", whereSql);

        String sql = "DELETE FROM ${table} WHERE ${where}";
        log.debug("DELETE DATA, table: {}, where: {}, params: {}", modelContext.getModel().getSchemaAndTableName(), whereSql, root);
        return modelContext.getMybatisHelper().update(modelContext.getSqlSession(), sql, root);
    }

    private int batchPhysicalDelete(DataManager<ID> dataManager, @NonNull Collection<ID> idList) {
        ModelContext modelContext = dataManager.getModelContext();
        Model model = dataManager.getModel();
        Permission permission = modelContext.getPermission();

        Condition condition = GroupCondition.and(permission != null ? permission.getDataRights() : null, IdUtil.getIdsCondition(model, idList));

        modelContext.getInterceptor().beforePhysicalDelete((DataManager) dataManager, idList);
        int n = deleteByCondition(modelContext, condition);
        modelContext.getInterceptor().afterPhysicalDelete((DataManager) dataManager, idList);

        for (ID id : idList) {
            modelContext.sendEvent(new PhysicalDeleteDataEvent<>(model, id));
        }
        return n;
    }

    private int logicDelete(DataManager<?> dataManager, @NonNull Object id) {
        ModelContext modelContext = dataManager.getModelContext();
        Model model = dataManager.getModel();
        Map<String, Field> fieldMap = modelContext.getFieldMap();

        if (!fieldMap.containsKey(Model.FIELD_DELETE_FLAG)) {
            throw new UnsupportedCommandException("模型[" + model.getName() + "]没有'" + Model.FIELD_DELETE_FLAG + "'字段，不支持逻辑删除操作");
        }
        Condition idCondition = IdUtil.getIdCondition(model, id);
        Condition condition = modelContext.getAdditionalCondition() == null ? idCondition : GroupCondition.and(modelContext.getAdditionalCondition(), idCondition);
        List<FieldValue> updateData = getLogicDeleteData(dataManager);

        modelContext.getInterceptor().beforeLogicDelete((DataManager) dataManager, id, updateData);
        int n = UpdateExecution.update(modelContext, updateData, condition);

        modelContext.getInterceptor().afterLogicDelete((DataManager) dataManager, id, updateData);
        modelContext.sendEvent(new LogicDeleteDataEvent<>(model, id));
        return n;
    }


    private int batchLogicDelete(DataManager<ID> dataManager, @NonNull Collection<ID> ids) {
        ModelContext modelContext = dataManager.getModelContext();
        Model model = modelContext.getModel();
        Map<String, Field> fieldMap = modelContext.getFieldMap();

        if (!fieldMap.containsKey(Model.FIELD_DELETE_FLAG)) {
            throw new UnsupportedCommandException("模型[" + model.getName() + "]没有'" + Model.FIELD_DELETE_FLAG + "'字段，不支持逻辑删除操作");
        }
        Condition idCondition = IdUtil.getIdsCondition(model, ids);
        Condition condition = modelContext.getAdditionalCondition() == null ? idCondition : GroupCondition.and(modelContext.getAdditionalCondition(), idCondition);
        List<FieldValue> updateData = getLogicDeleteData(dataManager);

        modelContext.getInterceptor().beforeLogicDelete((DataManager) dataManager, ids, updateData);
        int n = UpdateExecution.update(modelContext, updateData, condition);

        modelContext.getInterceptor().afterLogicDelete((DataManager) dataManager, ids, updateData);
        for (ID id : ids) {
            modelContext.sendEvent(new LogicDeleteDataEvent<>(model, id));
        }
        return n;
    }


    public static List<FieldValue> getLogicDeleteData(DataManager<?> dataManager) {
        ModelContext modelContext = dataManager.getModelContext();
        List<FieldValue> result = new ArrayList<>();

        result.add(new FieldValue(((BasicField) modelContext.getPermissionedField(Model.FIELD_DELETE_FLAG)), true));

        dataManager.getModel().getFields().stream()
                .map(field -> new Tuple<>(getFiller(modelContext, field, modelContext.getFillers()), field))
                .filter(tuple -> tuple.v1 != null)
                .sorted(Comparator.comparing(t -> t.v1.getOrder()))
                .forEach(tuple -> tuple.v1.logicDeleteFill(dataManager, (BasicField) tuple.v2, result));
        return result;
    }

}
