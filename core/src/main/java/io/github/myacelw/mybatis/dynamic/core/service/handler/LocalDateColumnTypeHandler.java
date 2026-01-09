package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

import java.time.LocalDate;

/**
 * LocalDate类型字段转列类型处理器
 *
 * @author liuwei
 */
public class LocalDateColumnTypeHandler implements ColumnTypeHandler{

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(LocalDate.class)) {
            column.setDataType("DATE");
            return true;
        }
        return false;
    }

}
