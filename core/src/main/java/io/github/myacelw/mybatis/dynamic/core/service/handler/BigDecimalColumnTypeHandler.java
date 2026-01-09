package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

import java.math.BigDecimal;

/**
 * BigDecimal类型字段转列类型处理器
 *
 * @author liuwei
 */
public class BigDecimalColumnTypeHandler implements ColumnTypeHandler{

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(BigDecimal.class)) {
            column.setDataType("NUMERIC");
            column.setNumericPrecision(20);
            column.setNumericScale(10);
            return true;
        }
        return false;
    }

}
