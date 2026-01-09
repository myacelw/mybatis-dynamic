package io.github.myacelw.mybatis.dynamic.core.service;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;

import java.util.List;

/**
 * 数据变更拦截器
 *
 * @author liuwei
 */
public interface DataChangeInterceptor {

    /**
     * 插入数据前
     */
    default void beforeInsert(DataManager<Object> dataManager, Object data) {
        // do nothing
    }

    /**
     * 主表、子表、关系表数据，全部插入完成后
     */
    default void afterInsert(DataManager<Object> dataManager, Object data, Object id) {
        // do nothing
    }

    /**
     * 数据更新前
     */
    default void beforeUpdate(DataManager<Object> dataManager, Object id, Object data, List<FieldValue> fieldValues) {
        // do nothing
    }

    /**
     * 主表、子表、关系表数据，全部更新完成后
     */
    default void afterUpdate(DataManager<Object> dataManager, Object id, Object data, List<FieldValue> fieldValues) {
        // do nothing
    }

    /**
     * 数据更新前
     */
    default void beforeUpdateByCondition(DataManager<Object> dataManager, Condition condition, Object data, List<FieldValue> fieldValues) {
        // do nothing
    }

    default void afterUpdateByCondition(DataManager<Object> dataManager, Condition condition, Object data, List<FieldValue> fieldValues) {
        // do nothing
    }

    default void beforeLogicDelete(DataManager<Object> dataManager, Object ids, List<FieldValue> updateData) {
        // do nothing
    }

    default void afterLogicDelete(DataManager<Object> dataManager, Object ids, List<FieldValue> updateData) {
        // do nothing
    }

    default void beforePhysicalDelete(DataManager<Object> dataManager, Object idOrIds) {
        // do nothing
    }

    default void beforePhysicalDeleteByCondition(DataManager<Object> dataManager, Condition condition) {
        // do nothing
    }

    default void afterPhysicalDeleteByCondition(DataManager<Object> dataManager, Condition condition) {
        // do nothing
    }

    default void beforeLogicDeleteByCondition(DataManager<Object> dataManager, Condition condition) {
        // do nothing
    }

    default void afterLogicDeleteByCondition(DataManager<Object> dataManager, Condition condition) {
        // do nothing
    }


    default void afterPhysicalDelete(DataManager<Object> dataManager, Object idOrIds) {
        // do nothing
    }

    default void beforeBatchInsert(DataManager<Object> dataManager, List<?> dataList) {

    }

    default void afterBatchInsert(DataManager<Object> dataManager, List<?> dataList, List<?> idList) {

    }

    default void beforeBatchUpdate(DataManager<Object> dataManager, List<?> dataList) {

    }

    default void afterBatchUpdate(DataManager<Object> dataManager, List<?> dataList) {

    }


    default void beforeBatchInsertOrUpdate(DataManager<Object> dataManager, List<?> dataList) {

    }

    default void afterBatchInsertOrUpdate(DataManager<Object> dataManager, List<?> dataList) {

    }
}
