package io.github.myacelw.mybatis.dynamic.core.exception.crud;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 查询参数异常。
 *
 * @author liuwei
 */
public class ConditionParameterException extends BaseException {

    public ConditionParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConditionParameterException(String message) {
        super(message);
    }
}
