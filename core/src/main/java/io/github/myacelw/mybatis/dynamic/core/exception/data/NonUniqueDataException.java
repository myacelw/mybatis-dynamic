package io.github.myacelw.mybatis.dynamic.core.exception.data;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 数据不唯一异常。
 *
 * @author liuwei
 */
public class NonUniqueDataException extends BaseException {

    public NonUniqueDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonUniqueDataException(String message) {
        super(message);
    }
}
