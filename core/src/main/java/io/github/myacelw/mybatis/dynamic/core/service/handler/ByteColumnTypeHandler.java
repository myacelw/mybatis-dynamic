package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

/**
 * byte类型字段转列类型处理器
 *
 * @author liuwei
 */
public class ByteColumnTypeHandler implements ColumnTypeHandler {

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(Byte.class) || javaType.equals(byte.class)) {  //最大值：127
            column.setDataType("NUMERIC");
            column.setNumericPrecision(3);
            column.setNumericScale(0);
            return true;
        }
        return false;
    }

}
