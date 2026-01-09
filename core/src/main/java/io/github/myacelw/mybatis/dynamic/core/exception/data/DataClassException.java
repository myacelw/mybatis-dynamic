package io.github.myacelw.mybatis.dynamic.core.exception.data;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 数据类异常。
 *
 * @author liuwei
 */
public class DataClassException extends BaseException {

    public DataClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataClassException(String message) {
        super(message);
    }
}
