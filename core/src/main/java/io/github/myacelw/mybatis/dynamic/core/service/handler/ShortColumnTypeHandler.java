package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

/**
 * short字段转列类型处理器，转换为Json类型
 *
 * @author liuwei
 */
public class ShortColumnTypeHandler implements ColumnTypeHandler {

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(Short.class) || javaType.equals(short.class)) {  //最大值：32767
            column.setDataType("NUMERIC");
            column.setNumericPrecision(5);
            column.setNumericScale(0);
            return true;
        }
        return false;
    }

}
