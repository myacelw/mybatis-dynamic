package io.github.myacelw.mybatis.dynamic.core.exception.crud;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * Join字段异常。
 *
 * @author liuwei
 */
public class JoinFieldException extends BaseException {

    public JoinFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public JoinFieldException(String message) {
        super(message);
    }
}
