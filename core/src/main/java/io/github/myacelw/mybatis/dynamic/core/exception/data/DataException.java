package io.github.myacelw.mybatis.dynamic.core.exception.data;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 数据异常。
 *
 * @author liuwei
 */
public class DataException extends BaseException {

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataException(String message) {
        super(message);
    }
}
