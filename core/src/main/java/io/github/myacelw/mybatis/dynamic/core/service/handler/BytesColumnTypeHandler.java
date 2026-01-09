package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.OracleDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.PostgresqlDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;

/**
 * byte[]类型字段转列类型处理器
 *
 * @author liuwei
 */
public class BytesColumnTypeHandler implements ColumnTypeHandler {

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(byte[].class)) {
            if (dialect instanceof OracleDataBaseDialect) {
                column.setDataType("BLOB");
            } else if (dialect instanceof PostgresqlDataBaseDialect) {
                column.setDataType("BYTEA");
            } else {
                column.setDataType("LONGBLOB");
            }
            return true;
        }
        return false;
    }

}
