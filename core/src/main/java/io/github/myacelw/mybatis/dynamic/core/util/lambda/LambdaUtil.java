package io.github.myacelw.mybatis.dynamic.core.util.lambda;

import io.github.myacelw.mybatis.dynamic.core.util.lambda.impl.IdeaProxyLambdaMeta;
import io.github.myacelw.mybatis.dynamic.core.util.lambda.impl.SetAccessibleAction;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 来源自mybatis-plus库中实现。
 * Lambda 解析工具类
 *
 * @author HCL, MieMie
 * @since 2018-05-10
 */
public final class LambdaUtil {

    /**
     * 通过get方法获取字段名
     */
    @SafeVarargs
    public static <T> List<String> names(SFunction<T, ?>... funcs) {
        return Arrays.stream(funcs).map(LambdaUtil::name).collect(Collectors.toList());
    }

    /**
     * 通过get方法获取字段名
     */
    public static <T> String name(SFunction<T, ?> func) {
        String methodName = LambdaUtil.extract(func);
        String s;
        if (methodName.startsWith("is")) {
            s = methodName.substring("is".length());
        } else {
            s = methodName.substring("get".length());
        }
        return firstToLowerCase(s);
    }

    /**
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @param <T>  类型，被调用的 Function 对象的目标类型
     * @return 返回解析后的结果
     */
    private static <T> String extract(SFunction<T, ?> func) {
        // 1. IDEA 调试模式下 lambda 表达式是一个代理
        if (func instanceof Proxy) {
            return IdeaProxyLambdaMeta.getImplMethodName((Proxy) func);
        }
        // 2. 反射读取
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            return ((SerializedLambda) AccessController.doPrivileged(new SetAccessibleAction<>(method)).invoke(func)).getImplMethodName();
        } catch (Throwable e) {
            // 3. 反射失败使用序列化的方式读取
            return (io.github.myacelw.mybatis.dynamic.core.util.lambda.impl.SerializedLambda.extract(func)).getImplMethodName();
        }
    }

    /**
     * 首字母转换小写
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    private static String firstToLowerCase(String param) {
        if (param == null || param.isEmpty()) {
            return param;
        }
        if (param.length() == 1) {
            return param.toLowerCase();
        }

        return param.substring(0, 1).toLowerCase() + param.substring(1);
    }

}
