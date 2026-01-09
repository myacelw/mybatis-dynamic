package io.github.myacelw.mybatis.dynamic.core.service.filler;

import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;

import java.util.List;

/**
 * 抽象的创建人填充器
 *
 * @author liuwei
 */
public abstract class AbstractCreatorFiller implements Filler {

    public static final String NAME = "CreatorFiller";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void insertFill(DataManager<?> dataManager, BasicField field, Object data, List<FieldValue> fieldValues) {
        setUser(field, data, fieldValues);
    }

    @Override
    public void updateFill(DataManager<?> dataManager, BasicField field, Object data, List<FieldValue> fieldValues) {
        //修改的时候不变更
        removeColumnValue(field, fieldValues);
    }

    @Override
    public void logicDeleteFill(DataManager<?> dataManager, BasicField field, List<FieldValue> fieldValues) {
        //删除的时候不变更
        removeColumnValue(field, fieldValues);
    }

    protected void setUser(BasicField field, Object data, List<FieldValue> fieldValues) {
        Object user = getCurrentUser();
        if (user != null) {
            setFieldValue(field, data, user);
            setColumnValue(field, fieldValues, user);
        } else {
            removeColumnValue(field, fieldValues);
        }
    }

    abstract protected String getCurrentUser();

}

