package io.github.myacelw.mybatis.dynamic.core.exception.model;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 模型异常。
 *
 * @author liuwei
 */
public class ModelException extends BaseException {


    public ModelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelException(String message) {
        super(message);
    }
}
