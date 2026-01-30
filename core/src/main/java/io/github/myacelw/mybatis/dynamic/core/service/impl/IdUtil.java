package io.github.myacelw.mybatis.dynamic.core.service.impl;

import io.github.myacelw.mybatis.dynamic.core.exception.data.DataRequiredCheckException;
import io.github.myacelw.mybatis.dynamic.core.exception.model.ModelException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.DataUtil;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;

import java.util.*;

/**
 * 主键工具
 *
 * @author liuwei
 */
public class IdUtil {

    /**
     * 获取主键条件
     *
     * @param model 模型
     * @param id    主键值
     * @return 主键条件
     */
    public static Condition getIdCondition(Model model, Object id) {
        Assert.notNull(model, "Model cannot be null");
        String[] primaryKeyFields = model.getPrimaryKeyFields();
        if (ObjectUtil.isEmpty(primaryKeyFields)) {
            throw new ModelException("Model [" + model.getName() + "] has no primary key fields configured");
        }

        if (primaryKeyFields.length == 1) {
            return SimpleCondition.eq(primaryKeyFields[0], id);
        } else {
            return getMultiIdCondition(model, id);
        }
    }

    private static Condition getMultiIdCondition(Model model, Object id) {
        String[] primaryKeyFields = model.getPrimaryKeyFields();

        Condition condition;
        Object[] pk = getMultiId(id);
        if (pk.length != primaryKeyFields.length) {
            throw new IllegalArgumentException("Model [" + model.getName() + "] has " + primaryKeyFields.length + " primary key fields, but " + pk.length + " parameters were provided");
        }
        SimpleCondition[] conditions = new SimpleCondition[primaryKeyFields.length];
        for (int i = 0; i < primaryKeyFields.length; i++) {
            conditions[i] = SimpleCondition.eq(primaryKeyFields[i], pk[i]);
        }
        condition = GroupCondition.and(conditions);
        return condition;
    }

    /**
     * 获取多主键值
     *
     * @param id 主键值
     * @return 主键值数组
     */
    public static Object[] getMultiId(Object id) {
        Object[] pk;
        if (id instanceof Collection) {
            pk = ((Collection<?>) id).toArray();
        } else if (id.getClass().isArray()) {
            pk = (Object[]) id;
        } else {
            throw new IllegalArgumentException("Model has composite primary keys, but the ID parameter is not a list or array");
        }
        return pk;
    }

    /**
     * 获取多主键条件
     *
     * @param model 模型
     * @param idList 主键值列表
     * @return 主键条件
     */
    public static Condition getIdsCondition(Model model, Collection<?> idList) {
        String[] primaryKeyFields = model.getPrimaryKeyFields();
        if (ObjectUtil.isEmpty(primaryKeyFields)) {
            throw new ModelException("Model [" + model.getName() + "] has no primary key fields configured");
        }
        return getIdsCondition(primaryKeyFields, idList);
    }

    /**
     * 获取多主键或条件
     *
     * @param fields 一个或多个字段
     * @param fieldValueList 字段值列表
     * @return 主键条件
     */
    public static Condition getIdsCondition(String[] fields, Collection<?> fieldValueList) {
        Assert.notEmpty(fields, "Fields cannot be empty");
        Assert.notEmpty(fieldValueList, "Field values cannot be empty");

        int fieldSize = fields.length;
        if (fieldSize == 1) {
            return SimpleCondition.eqOrIn(fields[0], fieldValueList instanceof List ? (List<?>) fieldValueList : new ArrayList<>(fieldValueList));

        } else {
            Map<Object, Map> fkIdTreeMap = getIdTreeMap(fieldValueList, fieldSize);
            return createMultiIdCondition(fields, 0, fkIdTreeMap);
        }
    }

    private static Map<Object, Map> getIdTreeMap(Collection<?> multiIdList, int fieldCount) {
        Map<Object, Map> idMap = new HashMap<>();
        for (Object o : multiIdList) {
            Object[] multiId = getMultiId(o);
            Map<Object, Map> map = idMap;
            for (int i = 0; i < fieldCount; i++) {
                if (i == multiId.length - 1) {
                    map.put(multiId[i], null); //最后一个
                } else {
                    map = map.computeIfAbsent(multiId[i], k -> new HashMap<>());
                }
            }
        }
        return idMap;
    }

    private static Condition createMultiIdCondition(String[] fields, int i, Map<Object, Map> idTreeMap) {
        if (i == fields.length - 1) {
            return SimpleCondition.eqOrIn(fields[i], new ArrayList<>(idTreeMap.keySet()));
        }
        if (idTreeMap.size() == 1) {
            Map.Entry<Object, Map> entry = idTreeMap.entrySet().iterator().next();
            return createIdCondition(fields, i, entry.getKey(), entry.getValue());
        }

        List<Condition> orList = new ArrayList<>();
        for (Map.Entry<Object, Map> e : idTreeMap.entrySet()) {
            orList.add(createIdCondition(fields, i, e.getKey(), e.getValue()));
        }
        return GroupCondition.or(orList.toArray(new Condition[0]));
    }


    private static Condition createIdCondition(String[] fields, int i, Object id, Map subMap) {
        Condition subCondition = createMultiIdCondition(fields, i + 1, subMap);
        return GroupCondition.and(SimpleCondition.eq(fields[i], id), subCondition);
    }

    public static Object getId(Model model, Object data) {
        return getId(model, data, false);
    }

    /**
     * 得到数据中的Id值，如果是多值ID则返回数组
     * @param model 模型
     * @param data 模型数据
     * @param throwExceptionIfNull 如果为true，当id为空时抛出异常
     * @return id值
     */
    public static Object getId(Model model, Object data, boolean throwExceptionIfNull) {
        Assert.notNull(model, "Model cannot be null");
        Assert.notNull(data, "Data cannot be null");
        String[] primaryKeyFields = model.getPrimaryKeyFields();
        if (ObjectUtil.isEmpty(primaryKeyFields)) {
            if (throwExceptionIfNull) {
                throw new ModelException("Model [" + model.getName() + "] has no primary key fields configured");
            }
            return null;
        }
        if (primaryKeyFields.length == 1) {
            Object id = DataUtil.getProperty(data, primaryKeyFields[0]);
            if (throwExceptionIfNull && ObjectUtil.isEmpty(id)) {
                throw new DataRequiredCheckException("Primary key value cannot be null");
            }
            return id;
        } else {
            Object[] multiId = new Object[primaryKeyFields.length];
            for (int i = 0; i < primaryKeyFields.length; i++) {
                multiId[i] = DataUtil.getProperty(data, primaryKeyFields[i]);
                if (throwExceptionIfNull && ObjectUtil.isEmpty(multiId[i])) {
                    throw new DataRequiredCheckException("Primary key value cannot be null");
                }
            }
            return multiId;
        }
    }

    /**
     * 判断id是否为空
     * @param id id
     * @return 是否为空
     */
    public static boolean isEmptyId(Object id) {
        if (ObjectUtil.isEmpty(id)) {
            return true;
        }

        if (id instanceof Collection) {
            for (Object o : (Collection<?>) id) {
                if (ObjectUtil.isEmpty(o)) {
                    return true;
                }
            }
        }
        if (id.getClass().isArray()) {
            for (Object o : (Object[]) id) {
                if (ObjectUtil.isEmpty(o)) {
                    return true;
                }
            }
        }
        return false;
    }

}
