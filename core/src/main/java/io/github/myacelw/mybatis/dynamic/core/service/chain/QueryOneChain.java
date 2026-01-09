package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryOneCommand;

/**
 * 查询一条数据处理链
 *
 * @author liuwei
 */
public class QueryOneChain<ID, T> extends AbstractSelectQueryChain<ID, T, T, QueryOneCommand<T>, QueryOneChain<ID, T>> {

    public QueryOneChain(DataManager<ID> dataManager) {
        super(dataManager, QueryOneCommand::new);
    }

    public QueryOneChain(DataManager<ID> dataManager, Class<T> clazz) {
        super(dataManager, QueryOneCommand::new, clazz);
    }

    public QueryOneChain<ID, T> nullThrowException() {
        command.setNullThrowException(true);
        return this;
    }

    public QueryOneChain<ID, T> nullThrowException(boolean nullThrowException) {
        command.setNullThrowException(nullThrowException);
        return this;
    }

    public QueryOneChain<ID, T> onlyFirst() {
        command.setOnlyFirst(true);
        return this;
    }

    public QueryOneChain<ID, T> onlyFirst(boolean onlyFirst) {
        command.setOnlyFirst(onlyFirst);
        return this;
    }


}
