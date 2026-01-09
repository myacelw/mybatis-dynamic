package io.github.myacelw.mybatis.dynamic.core.util.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 来源自mybatis-plus库中实现。
 * 支持序列化的 Function
 *
 * @author miemie
 * @since 2018-05-12
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {

}