package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

/**
 * double类型字段转列类型处理器
 *
 * @author liuwei
 */
public class DoubleColumnTypeHandler implements ColumnTypeHandler{

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(Double.class) || javaType.equals(double.class)) { //最大精度15-16位
            column.setDataType("NUMERIC");
            column.setNumericPrecision(16);
            column.setNumericScale(4);
            return true;
        }
        return false;
    }

}
