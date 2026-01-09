package io.github.myacelw.mybatis.dynamic.core.util;

/**
 * 断言工具
 *
 * @author liuwei
 */
public final class Assert {

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Object object, String message) {
        if (ObjectUtil.isEmpty(object)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasText(String string, String message) {
        if (!StringUtil.hasText(string)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new IllegalArgumentException(message);
        }
    }
}
