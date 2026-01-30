package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.exception.model.ModelException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.InsertOrUpdateCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.IdUtil;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插入或更新数据执行器
 *
 * @author liuwei
 */
public class InsertOrUpdateExecution<ID> extends AbstractExecution<ID, ID, InsertOrUpdateCommand> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return InsertOrUpdateCommand.class;
    }

    @Override
    public ID exec(InsertOrUpdateCommand command, DataManager<ID> dataManager) {
        Object data = command.getData();

        Model model = dataManager.getModel();
        String[] primaryKeyFields = model.getPrimaryKeyFields();
        if (ObjectUtil.isEmpty(primaryKeyFields)) {
            throw new ModelException("Model [" + model.getName() + "] has no primary key field configured");
        }

        ID id = (ID) IdUtil.getId(model, data);
        boolean haveId = !IdUtil.isEmptyId(id);

        if (haveId && existsById(dataManager, id)) {
            dataManager.update(id, data);
            return id;
        }

        // 如果是指定id插入数据，并且模型为逻辑删除；那么可能存在id相同的逻辑删除数据，此时需要将数据物理删除后再插入
        if (haveId && dataManager.getModel().isLogicDelete() && existsByIdIgnoreLogicDelete(dataManager, id)) {
            dataManager.delete(id, true);
        }
        return dataManager.insertDisableGenerateId(data);
    }

    /**
     * 检查对应id数据是否存在，忽略数据权限。
     */
    private boolean existsById(DataManager<?> dataManager, Object id) {
        List<?> list = dataManager.queryChain().where(IdUtil.getIdCondition(dataManager.getModel(), id)).limit(1).selectFieldsForId().exec();
        return !ObjectUtil.isEmpty(list);
    }

    protected boolean existsByIdIgnoreLogicDelete(DataManager<?> dataManager, Object id) {
        ModelContext modelContext = dataManager.getModelContext();
        Condition condition = IdUtil.getIdCondition(dataManager.getModel(), id);
        String whereSql = condition.sql("c", t -> convertColumnForAllField(modelContext, t), modelContext.getDialect());

        Map<String, Object> context = new HashMap<>();
        context.put("c", condition);
        context.put("table", modelContext.getModel().getSchemaAndTableName());
        context.put("whereSql", whereSql);

        String sql = "SELECT count(1) FROM ${table} WHERE ${whereSql}";

        int n = modelContext.getMybatisHelper().queryOne(modelContext.getSqlSession(), sql, context, Integer.class);
        return n > 0;
    }

}
