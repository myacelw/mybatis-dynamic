package io.github.myacelw.mybatis.dynamic.core.exception.crud;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * Join异常。
 *
 * @author liuwei
 */
public class JoinException extends BaseException {

    public JoinException(String message, Throwable cause) {
        super(message, cause);
    }

    public JoinException(String message) {
        super(message);
    }
}
