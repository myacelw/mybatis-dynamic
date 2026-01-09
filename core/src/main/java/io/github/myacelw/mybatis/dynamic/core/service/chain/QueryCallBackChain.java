package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCallBackCommand;
import org.apache.ibatis.session.ResultHandler;

/**
 * 查询回调处理链
 *
 * @author liuwei
 */
public class QueryCallBackChain<ID, T> extends AbstractSelectQueryChain<ID, T, Integer, QueryCallBackCommand<T>, QueryCallBackChain<ID, T>> {

    public QueryCallBackChain(DataManager<ID> dataManager) {
        super(dataManager, QueryCallBackCommand::new);
    }

    public QueryCallBackChain(DataManager<ID> dataManager, Class<T> clazz) {
        super(dataManager, QueryCallBackCommand::new, clazz);
    }

    /**
     * 设置分页
     */
    public QueryCallBackChain<ID, T> page(Page page) {
        command.setPage(page);
        return this;
    }

    public QueryCallBackChain<ID, T> page(int current, int size) {
        command.setPage(new Page(current, size));
        return this;
    }

    public QueryCallBackChain<ID, T> limit(int size) {
        command.setPage(new Page(1, size));
        return this;
    }

    /**
     * 设置回调处理器
     */
    public QueryCallBackChain<ID, T> handler(ResultHandler<T> handler) {
        command.setHandler(handler);
        return this;
    }

}
