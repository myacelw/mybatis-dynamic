package io.github.myacelw.mybatis.dynamic.spring.dao;

import io.github.myacelw.mybatis.dynamic.core.service.ModelService;
import io.github.myacelw.mybatis.dynamic.core.util.OrderUtil;
import io.github.myacelw.mybatis.dynamic.spring.entity.BaseTreeEntity;
import lombok.NonNull;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 基本树形实体的DAO实现类
 *
 * @author liuwei
 */
public class BaseTreeDaoImpl<ID, T> extends BaseDaoImpl<ID, T> implements BaseTreeDao<ID, T> {

    protected BaseTreeDaoImpl(Class<T> entityClass) {
        super(entityClass);
    }

    public BaseTreeDaoImpl(Class<T> entityClass, ModelService modelService, SqlSessionTemplate sqlSessionTemplate, Boolean updateMode) {
        super(entityClass, modelService, sqlSessionTemplate, updateMode);
    }

    @Override
    public ID insert(@NonNull T data) {
        setOrderString(data, OrderUtil.getLastOrderString());
        return super.insert(data);
    }

    @Override
    public ID insert(@NonNull T data, String previous, String next) {
        setOrderString(data, OrderUtil.getMiddleOrderString(previous, next));
        return super.insert(data);
    }

    @Override
    public ID insertToFirst(@NonNull T data) {
        setOrderString(data, OrderUtil.getFirstOrderString());
        return super.insert(data);
    }

    @Override
    public String moveToFirst(ID id) {
        Map<String, Object> data = new HashMap<>();
        String orderString = OrderUtil.getFirstOrderString();
        data.put(getOrderStringFieldName(), orderString);
        this.getDataManager().update(id, data);
        return orderString;
    }

    @Override
    public String moveToLast(ID id) {
        Map<String, Object> data = new HashMap<>();
        String orderString = OrderUtil.getLastOrderString();
        data.put(getOrderStringFieldName(), orderString);
        this.getDataManager().update(id, data);
        return orderString;
    }

    @Override
    public String move(ID id, String previous, String next) {
        Map<String, Object> data = new HashMap<>();
        String orderString = OrderUtil.getMiddleOrderString(previous, next);
        data.put(getOrderStringFieldName(), orderString);
        this.getDataManager().update(id, data);
        return orderString;
    }

    @Override
    public String moveToFirst(ID id, ID parentId) {
        Map<String, Object> data = new HashMap<>();
        String orderString = OrderUtil.getFirstOrderString();
        data.put(getOrderStringFieldName(), orderString);
        data.put(getParentFieldName(), parentId);
        this.getDataManager().update(id, data);
        return orderString;
    }

    @Override
    public String moveToLast(ID id, ID parentId) {
        Map<String, Object> data = new HashMap<>();
        String orderString = OrderUtil.getLastOrderString();
        data.put(getOrderStringFieldName(), orderString);
        data.put(getParentFieldName(), parentId);
        this.getDataManager().update(id, data);
        return orderString;
    }

    @Override
    public String move(ID id, ID parentId, String previous, String next) {
        Map<String, Object> data = new HashMap<>();
        String orderString = OrderUtil.getMiddleOrderString(previous, next);
        data.put(getOrderStringFieldName(), orderString);
        data.put(getParentFieldName(), parentId);
        this.getDataManager().update(id, data);
        return orderString;
    }

    @Override
    public ID insertOrUpdate(@NonNull T data) {
        setOrderString(data, OrderUtil.getLastOrderString());
        return getDataManager().insertOrUpdate(data);
    }

    protected void setOrderString(T data, String orderString) {
        if (data instanceof BaseTreeEntity) {
            BaseTreeEntity<?, ?> t = (BaseTreeEntity<?, ?>) data;
            if (!StringUtils.hasText(t.getOrderString())) {
                t.setOrderString(orderString != null ? orderString : OrderUtil.getLastOrderString());
            }
        }
    }

    protected String getOrderStringFieldName() {
        return BaseTreeEntity.Fields.orderString;
    }

    protected String getParentFieldName() {
        return BaseTreeEntity.Fields.parent;
    }

}

