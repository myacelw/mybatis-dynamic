package io.github.myacelw.mybatis.dynamic.spring.service;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.service.chain.QueryRecursiveListChain;
import io.github.myacelw.mybatis.dynamic.core.service.chain.QueryRecursiveTreeChain;
import lombok.NonNull;

import java.util.List;

/**
 * 树形实体的基本Service接口
 *
 * @author liuwei
 */
public interface BaseTreeService<ID, T> extends BaseService<ID, T> {

    /**
     * 插入数据到目标节点之间
     */
    ID insert(T data, String previous, String next);

    /**
     * 插入数据到最前面
     */
    ID insertToFirst(T data);

    /**
     * 移动数据到最前面
     *
     * @return 新的orderString
     */
    String moveToFirst(ID id);

    /**
     * 移动数据到最后面
     *
     * @return 新的orderString
     */
    String moveToLast(ID id);

    /**
     * 移动数据到目标节点之间
     *
     * @return 新的orderString
     */
    String move(ID id, String previous, String next);


    /**
     * 递归查询
     */
    QueryRecursiveListChain<ID, T> queryRecursiveList();


    List<T> queryRecursiveList(Condition initNodeCondition, boolean recursiveDown);

    /**
     * 向下递归查询，返回树形结构
     */
    QueryRecursiveTreeChain<ID, T> queryRecursiveTree();

    List<T> queryRecursiveTree(Condition initNodeCondition);

    List<T> queryRecursiveTree(List<OrderItem> orderItems);

    List<T> queryRecursiveTreeAll();

    /**
     * 按ID查询属性结构该节点及以下节点，并以树形结构返回
     */
    T getRecursiveTreeById(@NonNull ID id);

}

