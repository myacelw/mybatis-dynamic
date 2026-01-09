package io.github.myacelw.mybatis.dynamic.core.exception.crud;

import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;

/**
 * 查询权限异常。
 *
 * @author liuwei
 */
public class ConditionPermissionException extends BaseException {

    public ConditionPermissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConditionPermissionException(String message) {
        super(message);
    }
}
