package io.github.myacelw.mybatis.dynamic.core.exception.database;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * SQL异常。
 *
 * @author liuwei
 */
public class SqlException extends BaseException {

    public SqlException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlException(String message) {
        super(message);
    }
}
