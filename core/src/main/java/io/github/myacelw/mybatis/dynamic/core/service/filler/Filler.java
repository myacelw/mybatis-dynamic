package io.github.myacelw.mybatis.dynamic.core.service.filler;

import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.util.DataUtil;

import java.util.List;
import java.util.Map;

/**
 * 模型数据增删改查时字段值自动填充器接口
 *
 * @author liuwei
 */
public interface Filler {

    Filler[] DEFAULT_FILLERS = new Filler[]{new CreateTimeFiller(), new UpdateTimeFiller()};

    String getName();

    /**
     * 执行顺序，小的先执行
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 插入时填充值
     *
     * @param dataManager 数据管理器，可获得填充的模型
     * @param field       填充字段
     * @param data        插入的原始数据
     * @param fieldValues 数据库将要插入的数据
     */
    void insertFill(DataManager<?> dataManager, BasicField field, Object data, List<FieldValue> fieldValues);

    /**
     * 变更时填充值，不变更数据时应从result中移除对应字段值
     *
     * @param dataManager 数据管理器，可获得填充的模型
     * @param field       填充字段
     * @param data        修改的原始数据
     * @param fieldValues 数据库将要修改的数据
     */
    void updateFill(DataManager<?> dataManager, BasicField field, Object data, List<FieldValue> fieldValues);

    /**
     * 逻辑删除时填充，不变更数据时应从result中移除对应字段值
     *
     * @param dataManager 数据管理器，可获得填充的模型
     * @param field       填充字段
     * @param fieldValues 数据库将要修改的数据
     */
    void logicDeleteFill(DataManager<?> dataManager, BasicField field, List<FieldValue> fieldValues);

    /**
     * 填充对象属性值
     */
    default boolean setFieldValue(BasicField field, Object data, Object value) {
        if (data != null && (data instanceof Map || DataUtil.containsKey(data, field.getName()))) {
            try {
                DataUtil.setProperty(data, field.getName(), value);
                return true;
            } catch (UnsupportedOperationException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 填充变更表格列值
     */
    default void setColumnValue(BasicField field, List<FieldValue> fieldValues, Object value) {
        for (FieldValue fieldValue : fieldValues) {
            if (fieldValue.getField().getName().equals(field.getName())) {
                fieldValue.setValue(value);
                return;
            }
        }
        fieldValues.add(new FieldValue(field, value));
    }

    /**
     * 移除字段值
     */
    default void removeColumnValue(BasicField field, List<FieldValue> fieldValues) {
        fieldValues.removeIf(fieldValue -> fieldValue.getField().getName().equals(field.getName()));
    }


}
