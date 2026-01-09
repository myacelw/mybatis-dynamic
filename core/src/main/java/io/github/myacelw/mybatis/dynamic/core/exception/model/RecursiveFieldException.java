package io.github.myacelw.mybatis.dynamic.core.exception.model;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 自关联字段配置异常。
 *
 * @author liuwei
 */
public class RecursiveFieldException extends BaseException {

    public RecursiveFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecursiveFieldException(String message) {
        super(message);
    }
}
