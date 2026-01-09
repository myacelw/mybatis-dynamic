package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryOneRecursiveTreeCommand;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;

import java.util.function.Consumer;

/**
 * 查询一条树形结构数据处理链
 *
 * @author liuwei
 */
public class QueryOneRecursiveTreeChain<ID, T> extends AbstractSelectQueryChain<ID, T, T, QueryOneRecursiveTreeCommand<T>, QueryOneRecursiveTreeChain<ID, T>> {

    public QueryOneRecursiveTreeChain(DataManager<ID> dataManager) {
        super(dataManager, QueryOneRecursiveTreeCommand::new);
    }

    public QueryOneRecursiveTreeChain(DataManager<ID> dataManager, Class<T> clazz) {
        super(dataManager, QueryOneRecursiveTreeCommand::new, clazz);
    }

    public QueryOneRecursiveTreeChain<ID, T> nullThrowException() {
        command.setNullThrowException(true);
        return this;
    }

    /**
     * 递归的树主表初始条目查询的条件
     * 注意：条件中只有主表字段条件，不能使用关联表字段条件；当为空时查询所有数据不递归，因此此时 recursiveDown 无意义。
     */
    public QueryOneRecursiveTreeChain<ID, T> initNodeCondition(Condition initNodeCondition) {
        Assert.notNull(initNodeCondition, "initNodeCondition can not be null");
        command.setInitNodeCondition(initNodeCondition);
        return this;
    }

    public QueryOneRecursiveTreeChain<ID, T> initNodeCondition(Consumer<Condition.ConditionBuilder> initNodeCondition) {
        Assert.notNull(initNodeCondition, "initNodeCondition can not be null");
        Condition.ConditionBuilder builder = Condition.builder();
        initNodeCondition.accept(builder);
        command.setInitNodeCondition(builder.build());
        return this;
    }

}
