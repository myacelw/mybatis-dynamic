package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.database.MybatisHelper;
import io.github.myacelw.mybatis.dynamic.core.event.data.BatchUpdateByConditionDataEvent;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.BatchUpdateByConditionCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 按条件批量更新执行器
 *
 * @author conductor
 */
@Slf4j
public class BatchUpdateByConditionExecution<ID> extends AbstractExecution<ID, Void, BatchUpdateByConditionCommand> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return BatchUpdateByConditionCommand.class;
    }

    @Override
    public Void exec(BatchUpdateByConditionCommand command, DataManager<ID> dataManager) {
        ModelContext modelContext = dataManager.getModelContext();
        List<BatchUpdateByConditionCommand.UpdatePair> updates = command.getUpdates();
        if (updates == null || updates.isEmpty()) {
            return null;
        }

        List<MybatisHelper.BatchItem> batchItems = new ArrayList<>();
        for (BatchUpdateByConditionCommand.UpdatePair pair : updates) {
            List<FieldValue> fieldValues = UpdateExecution.getChangedFieldValues(modelContext, pair.getData(), pair.isUpdateOnlyNonNull(), true);
            UpdateExecution.fill(dataManager, pair.getData(), fieldValues);

            if (fieldValues.isEmpty() && (pair.getCustomSetList() == null || pair.getCustomSetList().isEmpty())) {
                log.debug("更新数据为空，跳过执行更新操作，model:{}", modelContext.getModel().getName());
                continue;
            }

            Map<String, Object> sqlContext = UpdateExecution.getSqlContext(modelContext, fieldValues, pair.getCustomSetList(), pair.getCondition());
            batchItems.add(new MybatisHelper.BatchItem(UpdateExecution.SQL, sqlContext));
        }

        if (batchItems.isEmpty()) {
            return null;
        }

        modelContext.getInterceptor().beforeBatchUpdateByCondition((DataManager) dataManager, updates);
        modelContext.getMybatisHelper().batchUpdates(batchItems, command.getBatchSize());
        modelContext.getInterceptor().afterBatchUpdateByCondition((DataManager) dataManager, updates);

        modelContext.sendEvent(new BatchUpdateByConditionDataEvent(modelContext.getModel(), updates));

        return null;
    }

}
