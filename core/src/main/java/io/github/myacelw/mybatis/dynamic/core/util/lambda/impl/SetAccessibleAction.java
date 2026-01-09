package io.github.myacelw.mybatis.dynamic.core.util.lambda.impl;

import java.lang.reflect.AccessibleObject;
import java.security.PrivilegedAction;

/**
 * 来源自mybatis-plus库中实现。
 * Create by hcl at 2021/5/14
 */
public class SetAccessibleAction<T extends AccessibleObject> implements PrivilegedAction<T> {
    private final T obj;

    public SetAccessibleAction(T obj) {
        this.obj = obj;
    }

    @Override
    public T run() {
        obj.setAccessible(true);
        return obj;
    }

}
