package io.github.myacelw.mybatis.dynamic.sample.mybatis;

import java.io.Closeable;
import java.util.List;

/**
 * SQL 执行监听器
 *
 * @author liuwei
 */
public interface SqlExecListener extends Closeable {

    /**
     * 在 SQL 执行前调用
     */
    void onBeforeExecute(String sql, List<Object> parameterValues);

    @Override
    default void close() {
        SqlExecutionInterceptor.sqlExecListenerThreadLocal.remove();
    }

    static <T extends SqlExecListener> T listen(T listener) {
        SqlExecutionInterceptor.sqlExecListenerThreadLocal.set(listener);
        return listener;
    }

}
