package io.github.myacelw.mybatis.dynamic.core.service.impl;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.service.DataChangeInterceptor;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据变更拦截器组
 *
 * @author liuwei
 */
public class DataChangeInterceptorGroup implements DataChangeInterceptor {

    @Getter
    private final List<DataChangeInterceptor> interceptors;

    public DataChangeInterceptorGroup(List<DataChangeInterceptor> interceptors) {
        this.interceptors = interceptors == null ? new ArrayList<>() : new ArrayList<>(interceptors);
    }

    /**
     * 插入数据前
     */
    public void beforeInsert(DataManager<Object> dataManager, Object data) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.beforeInsert(dataManager, data);
        }
    }

    /**
     * 主表、子表、关系表数据，全部插入完成后
     */
    public void afterInsert(DataManager<Object> dataManager, Object data, Object id) {
        //倒序调用
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            DataChangeInterceptor interceptor = interceptors.get(i);
            interceptor.afterInsert(dataManager, data, id);
        }
    }

    /**
     * 数据更新前
     */
    public void beforeUpdate(DataManager<Object> dataManager, Object id, Object data, List<FieldValue> fieldValues) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.beforeUpdate(dataManager, id, data, fieldValues);
        }
    }

    /**
     * 主表、子表、关系表数据，全部更新完成后
     */
    public void afterUpdate(DataManager<Object> dataManager, Object id, Object data, List<FieldValue> fieldValues) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            DataChangeInterceptor interceptor = interceptors.get(i);
            interceptor.afterUpdate(dataManager, id, data, fieldValues);
        }
    }

    /**
     * 数据更新前
     */
    public void beforeUpdateByCondition(DataManager<Object> dataManager, Condition condition, Object data, List<FieldValue> fieldValues) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.beforeUpdateByCondition(dataManager, condition, data, fieldValues);
        }
    }

    public void afterUpdateByCondition(DataManager<Object> dataManager, Condition condition, Object data, List<FieldValue> fieldValues) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            DataChangeInterceptor interceptor = interceptors.get(i);
            interceptor.afterUpdateByCondition(dataManager, condition, data, fieldValues);
        }
    }

    public void beforeLogicDelete(DataManager<Object> dataManager, Object mainTableIdOrIds, List<FieldValue> updateData) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.beforeLogicDelete(dataManager, mainTableIdOrIds, updateData);
        }
    }

    public void afterLogicDelete(DataManager<Object> dataManager, Object mainTableIdOrIds, List<FieldValue> updateData) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.afterLogicDelete(dataManager, mainTableIdOrIds, updateData);
        }
    }

    public void beforePhysicalDelete(DataManager<Object> dataManager, Object mainTableIdOrIds) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.beforePhysicalDelete(dataManager, mainTableIdOrIds);
        }
    }

    public void beforePhysicalDeleteByCondition(DataManager<Object> dataManager, Condition condition) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.beforePhysicalDeleteByCondition(dataManager, condition);
        }
    }

    public void afterPhysicalDeleteByCondition(DataManager<Object> dataManager, Condition condition) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.afterPhysicalDeleteByCondition(dataManager, condition);
        }
    }

    public void beforeLogicDeleteByCondition(DataManager<Object> dataManager, Condition condition) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.beforeLogicDeleteByCondition(dataManager, condition);
        }
    }

    public void afterLogicDeleteByCondition(DataManager<Object> dataManager, Condition condition) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.afterLogicDeleteByCondition(dataManager, condition);
        }
    }

    public void afterPhysicalDelete(DataManager<Object> dataManager, Object idOrIds) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.afterPhysicalDelete(dataManager, idOrIds);
        }
    }

    public void beforeBatchInsert(DataManager<Object> dataManager, List<?> dataList) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.beforeBatchInsert(dataManager, dataList);
        }
    }

    public void afterBatchInsert(DataManager<Object> dataManager, List<?> dataList, List<?> idList) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.afterBatchInsert(dataManager, dataList, idList);
        }
    }

    public void beforeBatchUpdate(DataManager<Object> dataManager, List<?> dataList) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.beforeBatchUpdate(dataManager, dataList);
        }
    }

    public void afterBatchUpdate(DataManager<Object> dataManager, List<?> dataList) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            DataChangeInterceptor interceptor = interceptors.get(i);
            interceptor.afterBatchUpdate(dataManager, dataList);
        }
    }

    public void beforeBatchUpdateByCondition(DataManager<Object> dataManager, List<?> updates) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.beforeBatchUpdateByCondition(dataManager, updates);
        }
    }

    public void afterBatchUpdateByCondition(DataManager<Object> dataManager, List<?> updates) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            DataChangeInterceptor interceptor = interceptors.get(i);
            interceptor.afterBatchUpdateByCondition(dataManager, updates);
        }
    }

    public void beforeBatchInsertOrUpdate(DataManager<Object> dataManager, List<?> dataList) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.beforeBatchInsertOrUpdate(dataManager, dataList);
        }
    }

    public void afterBatchInsertOrUpdate(DataManager<Object> dataManager, List<?> dataList) {
        for (DataChangeInterceptor interceptor : interceptors) {
            interceptor.afterBatchInsertOrUpdate(dataManager, dataList);
        }
    }


}
