package io.github.myacelw.mybatis.dynamic.core.util.lambda.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.AccessController;

/**
 * 来源自mybatis-plus库中实现。
 * 在 IDEA 的 Evaluate 中执行的 Lambda 表达式元数据需要使用该类处理元数据
 * <p>
 * Create by hcl at 2021/5/17
 */
public class IdeaProxyLambdaMeta  {
    private static final Field FIELD_MEMBER_NAME;
    private static final Field FIELD_MEMBER_NAME_NAME;

    static {
        try {
            Class<?> classDirectMethodHandle = Class.forName("java.lang.invoke.DirectMethodHandle");
            FIELD_MEMBER_NAME = AccessController.doPrivileged(new SetAccessibleAction<>(classDirectMethodHandle.getDeclaredField("member")));
            Class<?> classMemberName = Class.forName("java.lang.invoke.MemberName");
            FIELD_MEMBER_NAME_NAME = AccessController.doPrivileged(new SetAccessibleAction<>(classMemberName.getDeclaredField("name")));
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getImplMethodName(Proxy func) {
        InvocationHandler handler = Proxy.getInvocationHandler(func);
        try {
            Object dmh = AccessController.doPrivileged(new SetAccessibleAction<>(handler.getClass().getDeclaredField("val$target"))).get(handler);
            Object member = FIELD_MEMBER_NAME.get(dmh);
            return (String) FIELD_MEMBER_NAME_NAME.get(member);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


}
