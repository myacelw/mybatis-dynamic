package io.github.myacelw.mybatis.dynamic.spring.vo;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import lombok.Data;

import java.util.List;

/**
 * 查询请求对象
 *
 * @author liuwei
 */
@Data
public class QueryRequest {

    /**
     * 查询返回的字段，为null时返回全部有权限的字段
     */
    List<String> selectFields;

    /**
     * 查询条件
     */
    Condition condition;

    /**
     * 排序
     */
    List<OrderItem> orderItems;

    /**
     * 分页
     */
    Page page;

}
