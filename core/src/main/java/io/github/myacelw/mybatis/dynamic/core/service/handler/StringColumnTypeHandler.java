package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.MysqlDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.OracleDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;

/**
 * String字段转列类型处理器，转换为Json类型
 *
 * @author liuwei
 */
public class StringColumnTypeHandler implements ColumnTypeHandler {

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType == String.class) {
            convertStringColumn(column, field, 100, dialect);
            return true;
        }
        return false;
    }

    public static void convertStringColumn(Column column, BasicField field, int defaultMaxLength, DataBaseDialect dialect) {
        int maxLength = field.getColumnDefine().getCharacterMaximumLength() == null ? defaultMaxLength : field.getColumnDefine().getCharacterMaximumLength();
        column.setDataType(getStringDataType(maxLength, dialect));
        if (column.getDataType().endsWith("CHAR")) {
            column.setCharacterMaximumLength(maxLength);
        } else {
            column.setCharacterMaximumLength(null);
        }
    }

    private static String getStringDataType(int maxLength, DataBaseDialect dialect) {
        // MYSQL 数据库
        if (dialect instanceof MysqlDataBaseDialect) {
            if (maxLength <= 0 || maxLength > 16777215) {
                return "LONGTEXT";
            } else if (maxLength > 65535) {
                return "MEDIUMTEXT";
            } else if (maxLength > 2000) {
                return "TEXT";
            } else {
                return "VARCHAR";
            }
        }

        if (dialect instanceof OracleDataBaseDialect) {
            if (maxLength > 2000 || maxLength <= 0) {
                return "CLOB";
            } else {
                return "VARCHAR";
            }
        }

        // POSTGRESQL、H2 数据库 不区分 TEXT、LONGTXT
        if (maxLength > 2000 || maxLength <= 0) {
            return "TEXT";
        } else {
            return "VARCHAR";
        }


    }

}
