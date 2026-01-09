package io.github.myacelw.mybatis.dynamic.core.service.filler;

import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新时间填充器
 * @author liuwei
 */
public class UpdateTimeFiller extends CreateTimeFiller {

    public static final String NAME = "UpdateTimeFiller";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void updateFill(DataManager<?> dataManager, BasicField field, Object data, List<FieldValue> fieldValues) {
        setDate(field, data, fieldValues);
    }

    @Override
    public void logicDeleteFill(DataManager<?> dataManager, BasicField field, List<FieldValue> fieldValues) {
        Object value = LocalDateTime.now();
        setColumnValue(field, fieldValues, value);
    }
}
