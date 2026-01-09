package io.github.myacelw.mybatis.dynamic.core.util.tuple;

import lombok.Value;

/**
 * 三元组辅助类
 *
 * @author liuwei
 */
@Value
public class Tuple3<T1, T2, T3> {
    public T1 v1;
    public T2 v2;
    public T3 v3;
}
