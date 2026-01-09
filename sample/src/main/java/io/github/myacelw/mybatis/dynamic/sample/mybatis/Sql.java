package io.github.myacelw.mybatis.dynamic.sample.mybatis;

import lombok.Value;

import java.util.List;

/**
 * SQL语句
 *
 * @author liuwei
 */
@Value
public class Sql {
    String sql;
    List<Object> parameterValues;
}
