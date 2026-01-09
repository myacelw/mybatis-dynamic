package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

/**
 * float类型字段转列类型处理器
 *
 * @author liuwei
 */
public class FloatColumnTypeHandler implements ColumnTypeHandler{

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(Float.class) || javaType.equals(float.class)) { //最大精度7-8位
            column.setDataType("NUMERIC");
            column.setNumericPrecision(8);
            column.setNumericScale(2);
            return true;
        }
        return false;
    }

}
