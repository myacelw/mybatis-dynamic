package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.CountRecursiveCommand;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;

import java.util.function.Consumer;

/**
 * 递归查询返回树形结构链
 *
 * @author liuwei
 */
public class CountRecursiveChain<ID> extends AbstractCountChain<ID, CountRecursiveCommand, CountRecursiveChain<ID>> {

    public CountRecursiveChain(DataManager<ID> dataManager) {
        super(dataManager, CountRecursiveCommand::new);
    }

    /**
     * 递归的树主表初始条目查询的条件
     * 注意：条件中只有主表字段条件，不能使用关联表字段条件；当为空时查询所有数据不递归，因此此时 recursiveDown 无意义。
     */
    public CountRecursiveChain<ID> initNodeCondition(Condition initNodeCondition) {
        command.setInitNodeCondition(initNodeCondition);
        return this;
    }

    public CountRecursiveChain<ID> initNodeCondition(Consumer<Condition.ConditionBuilder> initNodeCondition) {
        Assert.notNull(initNodeCondition, "initNodeCondition can not be null");
        Condition.ConditionBuilder builder = Condition.builder();
        initNodeCondition.accept(builder);
        command.setInitNodeCondition(builder.build());
        return this;
    }

    public CountRecursiveChain<ID> recursiveDown(boolean recursiveDown) {
        command.setRecursiveDown(recursiveDown);
        return this;
    }

    public CountRecursiveChain<ID> recursiveDown() {
        command.setRecursiveDown(true);
        return this;
    }

    public CountRecursiveChain<ID> recursiveUp() {
        command.setRecursiveDown(false);
        return this;
    }
}
