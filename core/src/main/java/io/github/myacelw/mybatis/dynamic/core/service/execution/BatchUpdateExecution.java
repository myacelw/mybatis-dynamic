package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.event.data.BatchUpdateDataEvent;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.BatchUpdateCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.impl.IdUtil;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.myacelw.mybatis.dynamic.core.service.execution.UpdateExecution.getChangedFieldValues;

/**
 * 批量更新执行器
 *
 * @author liuwei
 */
@Slf4j
public class BatchUpdateExecution<ID> extends AbstractExecution<ID, Void, BatchUpdateCommand> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return BatchUpdateCommand.class;
    }

    @Override
    public Void exec(BatchUpdateCommand command, DataManager<ID> dataManager) {
        ModelContext modelContext = dataManager.getModelContext();
        Model model = modelContext.getModel();
        List<?> dataList = command.getData();
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }

        List<Object> sqlContextList = new ArrayList<>();
        for (Object data : dataList) {
            List<FieldValue> changedList = getChangedFieldValues(modelContext, data, command.isUpdateOnlyNonNull(), false);
            UpdateExecution.fill(dataManager, data, changedList);

            if (changedList.isEmpty()) {
                log.debug("更新数据为空，跳过执行更新操作，model:{}", model.getName());
                continue;
            }

            Object id = IdUtil.getId(model, data, true);
            Condition idCondition = IdUtil.getIdCondition(model, id);
            Condition condition = modelContext.getAdditionalCondition() == null ? idCondition : GroupCondition.and(modelContext.getAdditionalCondition(), idCondition);
            Map<String, Object> sqlContext = UpdateExecution.getSqlContext(modelContext, changedList, command.getCustomSetList(), condition);
            sqlContextList.add(sqlContext);
        }

        modelContext.getInterceptor().beforeBatchUpdate((DataManager) dataManager, dataList);
        modelContext.getMybatisHelper().batchUpdate(UpdateExecution.SQL, sqlContextList, command.getBatchSize());
        modelContext.getInterceptor().afterBatchUpdate((DataManager) dataManager, dataList);
        modelContext.sendEvent(new BatchUpdateDataEvent(dataManager.getModel(), dataList));
        return null;
    }

}
