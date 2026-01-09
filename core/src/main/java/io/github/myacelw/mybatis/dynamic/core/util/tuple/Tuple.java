package io.github.myacelw.mybatis.dynamic.core.util.tuple;

import lombok.Value;

/**
 * 二元组辅助类
 *
 * @author liuwei
 */
@Value
public class Tuple<T1, T2> {
    public T1 v1;
    public T2 v2;
}
