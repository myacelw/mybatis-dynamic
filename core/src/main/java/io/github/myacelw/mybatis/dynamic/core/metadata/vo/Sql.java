package io.github.myacelw.mybatis.dynamic.core.metadata.vo;

import lombok.Value;

/**
 * 带有执行上下文的Sql语句
 *
 * @author liuwei
 */
@Value
public class Sql {
    String sql;
    boolean ignoreError;

    public Sql(String sql) {
        this.sql = sql;
        this.ignoreError = false;
    }

    public Sql(String sql, boolean ignoreError) {
        this.sql = sql;
        this.ignoreError = ignoreError;
    }

}