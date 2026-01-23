package io.github.myacelw.mybatis.dynamic.core.service.chain;

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
     * 设置最大返回行数
     */
    public QueryCursorChain<ID, T> limit(Integer limit) {
        command.setLimit(limit);
        return this;
    }

    /**
     * 设置偏移量
     */
    public QueryCursorChain<ID, T> offset(Integer offset) {
        command.setOffset(offset);
        return this;
    }

}
