package io.github.myacelw.mybatis.dynamic.core.service.filler;

import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建时间填充器
 * @author liuwei
 */
public class CreateTimeFiller implements Filler{

    public static final String NAME = "CreateTimeFiller";

    @Override
    public String getName(){
        return NAME;
    }

    @Override
    public void insertFill(DataManager<?> dataManager, BasicField field, Object data, List<FieldValue> fieldValues) {
        setDate(field, data, fieldValues);
    }

    @Override
    public void updateFill(DataManager<?> dataManager, BasicField field,  Object data, List<FieldValue> fieldValues) {
        //修改的时候不变更
        removeColumnValue(field, fieldValues);
    }

    @Override
    public void logicDeleteFill(DataManager<?> dataManager, BasicField field, List<FieldValue> fieldValues) {
        //删除的时候不变更
        removeColumnValue(field, fieldValues);
    }

    protected void setDate(BasicField field, Object data, List<FieldValue> fieldValues) {
        Object value = LocalDateTime.now();
        setFieldValue(field, data, value);
        setColumnValue(field, fieldValues, value);
    }

}

