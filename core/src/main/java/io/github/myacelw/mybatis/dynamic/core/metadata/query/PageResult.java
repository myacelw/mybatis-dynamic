package io.github.myacelw.mybatis.dynamic.core.metadata.query;

import lombok.Value;

import java.util.List;

/**
 * 分页查询结果
 *
 * @author liuwei
 */
@Value
public class PageResult<T> {
    /**
     * 分页查询结果数据
     */
    List<T> data;

    /**
     * 总记录数
     */
    Integer total;
}