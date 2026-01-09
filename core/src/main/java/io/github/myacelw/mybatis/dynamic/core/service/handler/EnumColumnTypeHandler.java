package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

/**
 * 枚举类型字段转列类型处理器
 *
 * @author liuwei
 */
public class EnumColumnTypeHandler implements ColumnTypeHandler{

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (Enum.class.isAssignableFrom(javaType)) {
            column.setDataType("VARCHAR");
            column.setCharacterMaximumLength(50);
            return true;
        }
        return false;
    }

}
