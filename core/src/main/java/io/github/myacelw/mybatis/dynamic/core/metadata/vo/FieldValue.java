package io.github.myacelw.mybatis.dynamic.core.metadata.vo;

import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 字段和值
 *
 * @author liuwei
 */
@Data
@AllArgsConstructor
public class FieldValue {
    BasicField field;

    Object value;

    @Override
    public String toString() {
        return field.getColumnName() + "=" + value;
    }

}
