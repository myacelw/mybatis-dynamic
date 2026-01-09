package io.github.myacelw.mybatis.dynamic.spring.dao;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.ConditionBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.chain.QueryRecursiveListChain;
import io.github.myacelw.mybatis.dynamic.core.service.chain.QueryRecursiveTreeChain;
import lombok.NonNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * 基本树形实体的DAO接口
 *
 * @author liuwei
 */
public interface BaseTreeDao<ID, T> extends BaseDao<ID, T> {

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
     * 移动数据到最前面，同时改变父节点
     *
     * @return 新的orderString
     */
    String moveToFirst(ID id, ID parentId);

    /**
     * 移动数据到最后面，同时改变父节点
     *
     * @return 新的orderString
     */
    String moveToLast(ID id, ID parentId);

    /**
     * 移动数据到目标节点之间，同时改变父节点
     *
     * @return 新的orderString
     */
    String move(ID id, ID parentId, String previous, String next);

    /**
     * 递归查询，各层级返回到一个列表，而不是组成树结构
     */
    default QueryRecursiveListChain<ID, T> queryRecursiveList() {
        return new QueryRecursiveListChain<>(getDataManager(), getEntityClass());
    }

    /**
     * 递归查询，各层级返回到一个列表，而不是组成树结构
     */
    default List<T> queryRecursiveList(Condition initNodeCondition, boolean recursiveDown) {
        return queryRecursiveList().initNodeCondition(initNodeCondition).recursiveDown(recursiveDown).exec();
    }

    default List<T> queryRecursiveList(Consumer<ConditionBuilder> conditionBuilderConfig, boolean recursiveDown) {
        return queryRecursiveList().initNodeCondition(conditionBuilderConfig).recursiveDown(recursiveDown).exec();
    }

    /**
     * 向下递归查询，返回树形结构
     */
    default QueryRecursiveTreeChain<ID, T> queryRecursiveTree() {
        return new QueryRecursiveTreeChain<>(getDataManager(), getEntityClass());
    }

    /**
     * 递归查询，各层级数据组织成树
     */
    default List<T> queryRecursiveTree(Condition initNodeCondition) {
        return queryRecursiveTree().initNodeCondition(initNodeCondition).exec();
    }

    /**
     * 递归查询，各层级数据组织成树
     */
    default List<T> queryRecursiveTree(Consumer<ConditionBuilder> initNodeCondition) {
        return queryRecursiveTree().initNodeCondition(initNodeCondition).exec();
    }

    /**
     * 递归查询，各层级数据组织成树
     */
    default List<T> queryRecursiveTree(List<OrderItem> orderItems) {
        return queryRecursiveTree().orderItems(orderItems).exec();
    }

    /**
     * 递归查询，各层级数据组织成树
     */
    default List<T> queryRecursiveTreeAll() {
        return queryRecursiveTree().exec();
    }

    /**
     * 树形递归查询统计数据
     * @param initNodeCondition 递归查询主表初始条目的查询条件
     * @param recursiveDown 向下递归还是向上递归
     */
    default long countRecursive(Consumer<ConditionBuilder> initNodeCondition, boolean recursiveDown) {
        return getDataManager().countRecursive(initNodeCondition, recursiveDown);
    }

    /**
     * 树形递归查询统计数据
     * @param initNodeCondition 递归查询主表初始条目的查询条件
     * @param condition 递归查询后结果的查询条件，可使用关联表字段条件
     * @param recursiveDown 向下递归还是向上递归
     */
    default long countRecursive(Consumer<ConditionBuilder> initNodeCondition, Consumer<ConditionBuilder> condition, boolean recursiveDown) {
        return getDataManager().countRecursive(initNodeCondition, condition, recursiveDown);
    }

    /**
     * 按ID查询属性结构该节点及以下节点，并以树形结构返回
     */
    default T getRecursiveTreeById(@NonNull ID id) {
        return getDataManager().getRecursiveTreeById(id, getEntityClass());
    }

}

