package io.github.myacelw.mybatis.dynamic.core.exception.database;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 表异常。
 *
 * @author liuwei
 */
public class TableException extends BaseException {

    public TableException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableException(String message) {
        super(message);
    }
}
