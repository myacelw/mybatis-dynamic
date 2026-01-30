package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.event.data.LogicDeleteByConditionEvent;
import io.github.myacelw.mybatis.dynamic.core.event.data.PhysicalDeleteByConditionEvent;
import io.github.myacelw.mybatis.dynamic.core.exception.crud.ConditionParameterException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.Permission;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.DeleteByConditionCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 按条件删除数据执行器
 *
 * @author liuwei
 */
@Slf4j
public class DeleteByConditionExecution<ID> extends AbstractExecution<ID, Integer, DeleteByConditionCommand> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return DeleteByConditionCommand.class;
    }

    @Override
    public Integer exec(DeleteByConditionCommand command, DataManager<ID> dataManager) {
        if (ObjectUtil.isEmpty(command.getCondition())) {
            throw new ConditionParameterException("Delete data by condition: query condition cannot be empty");
        }

        boolean physicalDelete = !dataManager.getModelContext().isLogicDelete();

        if (command.isForcePhysicalDelete() || physicalDelete) {
            return physicalDelete(dataManager, command.getCondition());
        } else {
            return logicDelete(dataManager, command.getCondition());
        }
    }

    private int physicalDelete(DataManager<ID> dataManager, @NonNull Condition condition1) {
        ModelContext modelContext = dataManager.getModelContext();
        Model model = modelContext.getModel();

        //这里忽略了逻辑删除条件
        Condition condition = GroupCondition.and(modelContext.getPermission() != null ? modelContext.getPermission().getDataRights() : null, condition1);

        modelContext.getInterceptor().beforePhysicalDeleteByCondition((DataManager) dataManager, condition1);
        int n = DeleteExecution.deleteByCondition(modelContext, condition);
        ;
        modelContext.getInterceptor().afterPhysicalDeleteByCondition((DataManager) dataManager, condition1);
        modelContext.sendEvent(new PhysicalDeleteByConditionEvent(model, condition1));
        return n;
    }

    private int logicDelete(DataManager<ID> dataManager, @NonNull Condition condition1) {
        ModelContext modelContext = dataManager.getModelContext();
        Model model = modelContext.getModel();
        Permission permission = modelContext.getPermission();
        Map<String, Field> fieldMap = modelContext.getFieldMap();

        Condition condition = GroupCondition.and(permission != null ? permission.getDataRights() : null, condition1);

        modelContext.getInterceptor().beforeLogicDeleteByCondition((DataManager) dataManager, condition1);

        List<FieldValue> updateData = DeleteExecution.getLogicDeleteData(dataManager);

        int n = UpdateExecution.update(modelContext, updateData, condition);

        modelContext.getInterceptor().afterLogicDeleteByCondition((DataManager) dataManager, condition1);
        modelContext.sendEvent(new LogicDeleteByConditionEvent(model, condition1));
        return n;
    }

}
