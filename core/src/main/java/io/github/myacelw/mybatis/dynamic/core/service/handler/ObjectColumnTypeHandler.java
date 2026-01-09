package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.typehandler.JsonTypeHandler;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

/**
 * 其他类型字段转列类型处理器，转换为Json类型
 *
 * @author liuwei
 */
public class ObjectColumnTypeHandler implements ColumnTypeHandler {

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        StringColumnTypeHandler.convertStringColumn(column, field, 65535, dialect);

        if (field.getTypeHandlerClass() != null) {
            column.setTypeHandler(field.getTypeHandlerClass());
        } else {
            column.setTypeHandler(JsonTypeHandler.class);
            field.setTypeHandlerClass(JsonTypeHandler.class);
        }

        return true;
    }


}
