package io.github.myacelw.mybatis.dynamic.core.exception.data;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 数据是否必填检测异常。
 *
 * @author liuwei
 */
public class DataRequiredCheckException extends BaseException {

    public DataRequiredCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataRequiredCheckException(String message) {
        super(message);
    }
}
