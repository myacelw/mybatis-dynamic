package io.github.myacelw.mybatis.dynamic.core.exception;

/**
 * 基础异常类。
 *
 * @author liuwei
 */
public abstract class BaseException extends RuntimeException {

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(String message) {
        super(message);
    }
}
