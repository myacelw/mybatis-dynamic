package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;

/**
 * char字段转列类型处理器，转换为Json类型
 *
 * @author liuwei
 */
public class CharColumnTypeHandler implements ColumnTypeHandler {

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(Character.class) || javaType.equals(char.class)) {
            column.setDataType("CHAR");
            column.setCharacterMaximumLength(1);
            return true;
        }
        return false;
    }

}
