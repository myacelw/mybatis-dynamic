package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.ConditionBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.BatchUpdateByConditionCommand;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * 按条件批量更新数据处理链
 *
 * @author conductor
 */
public class BatchUpdateByConditionChain<ID> extends AbstractChain<ID, Void, BatchUpdateByConditionCommand, BatchUpdateByConditionChain<ID>> {

    public BatchUpdateByConditionChain(DataManager<ID> dataManager) {
        super(dataManager, () -> new BatchUpdateByConditionCommand(new ArrayList<>(), 1000));
    }

    /**
     * 添加一个更新操作
     *
     * @param condition 条件
     * @param data      更新数据
     */
    public BatchUpdateByConditionChain<ID> add(@NonNull Condition condition, @NonNull Object data) {
        return add(condition, data, false);
    }

    /**
     * 添加一个更新操作
     *
     * @param conditionBuilder 条件构建器
     * @param data             更新数据
     */
    public BatchUpdateByConditionChain<ID> add(@NonNull Consumer<ConditionBuilder> conditionBuilder, @NonNull Object data) {
        return add(conditionBuilder, data, false);
    }

    /**
     * 添加一个更新操作
     *
     * @param condition         条件
     * @param data              更新数据
     * @param updateOnlyNonNull 是否只更新非空字段
     */
    public BatchUpdateByConditionChain<ID> add(@NonNull Condition condition, @NonNull Object data, boolean updateOnlyNonNull) {
        command.getUpdates().add(new BatchUpdateByConditionCommand.UpdatePair(condition, data, updateOnlyNonNull, null));
        return self();
    }

    /**
     * 添加一个更新操作
     *
     * @param conditionBuilder  条件构建器
     * @param data              更新数据
     * @param updateOnlyNonNull 是否只更新非空字段
     */
    public BatchUpdateByConditionChain<ID> add(@NonNull Consumer<ConditionBuilder> conditionBuilder, @NonNull Object data, boolean updateOnlyNonNull) {
        ConditionBuilder builder = Condition.builder();
        conditionBuilder.accept(builder);
        return add(builder.build(), data, updateOnlyNonNull);
    }

    /**
     * 设置批量更新的大小
     */
    public BatchUpdateByConditionChain<ID> batchSize(int batchSize) {
        command.setBatchSize(batchSize);
        return self();
    }

}
