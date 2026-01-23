package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCommand;

import java.util.List;

/**
 * 查询处理链
 *
 * @author liuwei
 */
public class QueryChain<ID, T> extends AbstractSelectQueryChain<ID, T, List<T>, QueryCommand<T>, QueryChain<ID, T>> {

    public QueryChain(DataManager<ID> dataManager) {
        super(dataManager, QueryCommand::new);
    }

    public QueryChain(DataManager<ID> dataManager, Class<T> clazz) {
        super(dataManager, QueryCommand::new, clazz);
    }

    /**
     * 设置分页
     */
    public QueryChain<ID, T> page(Page page) {
        command.setPage(page);
        return this;
    }

    public QueryChain<ID, T> page(int current, int size) {
        command.setPage(new Page(current, size));
        return this;
    }

    public QueryChain<ID, T> limit(int size) {
        command.setPage(new Page(1, size));
        return this;
    }

}
