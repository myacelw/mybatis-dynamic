package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCursorCommand;
import org.apache.ibatis.cursor.Cursor;

/**
 * 游标查询处理链
 *
 * @author liuwei
 */
public class QueryCursorChain<ID, T> extends AbstractSelectQueryChain<ID, T, Cursor<T>, QueryCursorCommand<T>, QueryCursorChain<ID, T>> {

    public QueryCursorChain(DataManager<ID> dataManager) {
        super(dataManager, QueryCursorCommand::new);
    }

    public QueryCursorChain(DataManager<ID> dataManager, Class<T> clazz) {
        super(dataManager, QueryCursorCommand::new, clazz);
    }

    /**
     * 设置分页
     */
    public QueryCursorChain<ID, T> page(Page page) {
        command.setPage(page);
        return this;
    }

    public QueryCursorChain<ID, T> page(int current, int size) {
        command.setPage(new Page(current, size));
        return this;
    }

    public QueryCursorChain<ID, T> limit(int size) {
        command.setPage(new Page(1, size));
        return this;
    }

}
