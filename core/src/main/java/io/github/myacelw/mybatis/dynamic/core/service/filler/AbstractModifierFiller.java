package io.github.myacelw.mybatis.dynamic.core.service.filler;

import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;

import java.util.List;

/**
 * 抽象的修改人填充器
 * @author liuwei
 */
public abstract class AbstractModifierFiller extends AbstractCreatorFiller {

    public static final String NAME = "ModifierFiller";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void updateFill(DataManager<?> dataManager, BasicField field, Object data, List<FieldValue> fieldValues) {
        setUser(field, data, fieldValues);
    }

    @Override
    public void logicDeleteFill(DataManager<?> dataManager, BasicField field, List<FieldValue> fieldValues) {
        setUser(field, null, fieldValues);
    }

}

