package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

/**
 * Class类型字段转列类型处理器
 *
 * @author liuwei
 */
public class ClassColumnTypeHandler implements ColumnTypeHandler{

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType == Class.class) {
            StringColumnTypeHandler.convertStringColumn(column, field, 200, dialect);
            return true;
        }
        return false;
    }

}
