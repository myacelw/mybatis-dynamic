package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.event.data.UpdateByConditionDataEvent;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.UpdateByConditionCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 按条件更新数据执行器
 *
 * @author liuwei
 */
@Slf4j
public class UpdateByConditionExecution<ID> extends AbstractExecution<ID, Integer, UpdateByConditionCommand> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return UpdateByConditionCommand.class;
    }

    @Override
    public Integer exec(UpdateByConditionCommand command, DataManager<ID> dataManager) {
        ModelContext modelContext = dataManager.getModelContext();
        Model model = modelContext.getModel();

        if (ObjectUtil.isEmpty(command.getCondition())) {
            log.debug("更新数据为空，跳过执行更新操作，model:{}", model.getName());
        }
        Object data = command.getData();
        if (data instanceof Map && ((Map<?, ?>) data).isEmpty()) {
            log.info("更新数据为空，跳过执行更新操作");
            return 0;
        }

        List<FieldValue> fieldValues = UpdateExecution.getChangedFieldValues(modelContext, data, command.isOnlyUpdateNonNull(), true);

        if (fieldValues.isEmpty()) {
            log.debug("更新数据为空，跳过执行更新操作，model:{}", model.getName());
        }

        modelContext.getInterceptor().beforeUpdateByCondition((DataManager) dataManager, command.getCondition(), data, fieldValues);
        int n = UpdateExecution.update(modelContext, fieldValues, command.getCustomSetList(), command.getCondition());
        modelContext.getInterceptor().afterUpdateByCondition((DataManager) dataManager, command.getCondition(), data, fieldValues);

        modelContext.sendEvent(new UpdateByConditionDataEvent(model, command.getCondition(), data));

        return n;
    }

}
