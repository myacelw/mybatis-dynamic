package io.github.myacelw.mybatis.dynamic.core.exception.crud;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 不支持的命令异常。
 *
 * @author liuwei
 */
public class UnsupportedCommandException extends BaseException {

    public UnsupportedCommandException(String message) {
        super(message);
    }

    public UnsupportedCommandException(String message, Throwable cause) {
        super(message, cause);
    }

}