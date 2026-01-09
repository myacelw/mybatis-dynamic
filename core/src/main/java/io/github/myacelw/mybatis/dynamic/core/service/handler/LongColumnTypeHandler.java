package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.MysqlDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.PostgresqlDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

/**
 * long类型字段转列类型处理器
 *
 * @author liuwei
 */
public class LongColumnTypeHandler implements ColumnTypeHandler {

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(Long.class) || javaType.equals(long.class)) {
            if (dialect instanceof PostgresqlDataBaseDialect || dialect instanceof MysqlDataBaseDialect) {
                column.setDataType("BIGINT");
                return true;
            }
            column.setDataType("NUMERIC");
            column.setNumericPrecision(19);
            column.setNumericScale(0);
            return true;
        }
        return false;
    }

}
