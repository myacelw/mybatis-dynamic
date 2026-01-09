package io.github.myacelw.mybatis.dynamic.core.exception.crud;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 字段参数异常。
 *
 * @author liuwei
 */
public class FieldParameterException extends BaseException {

    public FieldParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public FieldParameterException(String message) {
        super(message);
    }
}
