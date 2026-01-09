package io.github.myacelw.mybatis.dynamic.sample.mybatis;

import lombok.Getter;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL 收集器
 *
 * @author liuwei
 */
public class SqlExecCollector implements SqlExecListener, Closeable {

    @Getter
    private final List<Sql> sqlList = new ArrayList<>();

    private SqlExecCollector() {
    }

    /**
     * 在 SQL 执行前调用
     */
    public void onBeforeExecute(String sql, List<Object> parameterValues) {
        sqlList.add(new Sql(sql, parameterValues));
    }

    public static SqlExecCollector build() {
        return SqlExecListener.listen(new SqlExecCollector());
    }
}
