package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

import java.math.BigInteger;

/**
 * BigInteger类型字段转列类型处理器
 *
 * @author liuwei
 */
public class BigIntegerColumnTypeHandler implements ColumnTypeHandler{

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(BigInteger.class)) {
            column.setDataType("NUMERIC");
            column.setNumericPrecision(30);
            return true;
        }
        return false;
    }

}
