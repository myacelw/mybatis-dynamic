package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.MysqlDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * LocalDateTime类型字段转列类型处理器
 *
 * @author liuwei
 */
public class LocalDateTimeColumnTypeHandler implements ColumnTypeHandler {

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(LocalDateTime.class) || javaType.equals(Date.class)) {
            if (dialect instanceof MysqlDataBaseDialect) {
                column.setDataType("DATETIME");
            } else {
                column.setDataType("TIMESTAMP");
            }
            return true;
        }
        return false;
    }

}
