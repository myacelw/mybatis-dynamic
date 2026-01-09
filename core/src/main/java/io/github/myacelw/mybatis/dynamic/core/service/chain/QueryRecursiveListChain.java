package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryRecursiveListCommand;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;

import java.util.List;
import java.util.function.Consumer;

/**
 * 递归查询返回列表链
 *
 * @author liuwei
 */
public class QueryRecursiveListChain<ID, T> extends AbstractSelectQueryChain<ID, T, List<T>, QueryRecursiveListCommand<T>, QueryRecursiveListChain<ID, T>> {

    public QueryRecursiveListChain(DataManager<ID> dataManager) {
        super(dataManager, QueryRecursiveListCommand::new);
    }

    public QueryRecursiveListChain(DataManager<ID> dataManager, Class<T> clazz) {
        super(dataManager, QueryRecursiveListCommand::new, clazz);
    }


    /**
     * 递归的树主表初始条目查询的条件
     * 注意：条件中只有主表字段条件，不能使用关联表字段条件；当为空时查询所有数据不递归，因此此时 recursiveDown 无意义。
     */
    public QueryRecursiveListChain<ID, T> initNodeCondition(Condition initNodeCondition) {
        Assert.notNull(initNodeCondition, "conditionBuilderConfig can not be null");
        command.setInitNodeCondition(initNodeCondition);
        return this;
    }

    public QueryRecursiveListChain<ID, T> initNodeCondition(Consumer<Condition.ConditionBuilder> initNodeCondition) {
        Assert.notNull(initNodeCondition, "initNodeCondition can not be null");
        Condition.ConditionBuilder builder = Condition.builder();
        initNodeCondition.accept(builder);
        command.setInitNodeCondition(builder.build());
        return this;
    }

    /**
     * 设置分页
     */
    public QueryRecursiveListChain<ID, T> page(Page page) {
        command.setPage(page);
        return this;
    }

    public QueryRecursiveListChain<ID, T> page(int current, int size) {
        command.setPage(new Page(current, size));
        return this;
    }

    public QueryRecursiveListChain<ID, T> limit(int size) {
        command.setPage(new Page(1, size));
        return this;
    }

    public QueryRecursiveListChain<ID, T> recursiveDown(boolean recursiveDown) {
        command.setRecursiveDown(recursiveDown);
        return this;
    }

    public QueryRecursiveListChain<ID, T> recursiveDown() {
        command.setRecursiveDown(true);
        return this;
    }

    public QueryRecursiveListChain<ID, T> recursiveUp() {
        command.setRecursiveDown(false);
        return this;
    }

}
