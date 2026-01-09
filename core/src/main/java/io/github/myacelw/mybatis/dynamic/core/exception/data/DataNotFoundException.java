package io.github.myacelw.mybatis.dynamic.core.exception.data;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 数据没有找到异常。
 *
 * @author liuwei
 */
public class DataNotFoundException extends BaseException {

    public DataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataNotFoundException(String message) {
        super(message);
    }
}
