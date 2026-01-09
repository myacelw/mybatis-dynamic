package io.github.myacelw.mybatis.dynamic.core.exception;

/**
 * 系统异常。
 *
 * @author liuwei
 */
public class SystemException extends BaseException {

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(String message) {
        super(message);
    }
}
