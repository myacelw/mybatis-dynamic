package io.github.myacelw.mybatis.dynamic.core.database.dialect;

import io.github.myacelw.mybatis.dynamic.core.metadata.enums.AlterOrDropStrategy;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.KeyGeneratorMode;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.Sql;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 抽象的数据库方言
 *
 * @author liuwei
 */
@Slf4j
public abstract class AbstractDataBaseDialect implements DataBaseDialect {

    /**
     * 关键字
     */
    public static final Set<String> KEYWORD = new HashSet<>(Arrays.asList(
            // A
            "ABSOLUTE", "ACTION", "ADD", "ADMINDB", "ALL", "ALLOCATE", "ALPHANUMERIC", "ALTER", "AND", "ANY", "ARE", "AS", "ASC",
            "ASSERTION", "AT", "AUTHORIZATION", "AUTOINCREMENT", "AVG",
            // B
            "BAND", "BEGIN", "BETWEEN", "BINARY", "BIT", "BIT_LENGTH", "BNOT", "BOR", "BOTH", "BXOR", "BY", "BYTE",
            // C
            "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHAR_LENGTH", "CHARACTER_LENGTH", "CHECK",
            "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "COMP", "COMPRESSION", "CONNECT", "CONNECTION",
            "CONSTRAINT", "CONSTRAINTS", "CONTAINER", "CONTINUE", "CONVERT", "CORRESPONDING", "COUNT", "COUNTER", "CREATE",
            "CREATEDB", "CROSS", "CURRENCY", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER",
            "CURSOR", "CALL", "CONDITION",
            // D
            "DATABASE", "DATE", "DATETIME", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE",
            "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DESCRIPTOR", "DIAGNOSTICS", "DISALLOW", "DISCONNECT",
            "DISTINCT", "DOMAIN", "DOUBLE", "DROP",
            // E
            "ELSE", "END", "END-EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXCLUSIVECONNECT", "EXEC", "EXECUTE",
            "EXISTS", "EXTERNAL", "EXTRACT",
            // F
            "FALSE", "FETCH", "FIRST", "FLOAT", "FLOAT4", "FLOAT8", "FOR", "FOREIGN", "FOUND", "FROM", "FULL",
            // G
            "GENERAL", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "GUID",
            // H
            "HAVING", "HOUR",
            // I
            "IDENTITY", "IEEEDOUBLE", "EEESINGLE", "IGNORE", "IMAGE", "IMMEDIATE", "IN", "INDEX", "INDICATOR", "INHERITABLE",
            "INITIALLY", "INNER", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTEGERT", "INTEGER2", "INTEGER4",
            "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION",
            // J
            "JOIN",
            // K
            "KEY",
            // L
            "LANGUAGE", "LAST", "LEADING", "LEFT", "LEVEL", "LIKE", "LOCAL", "LOGICAL", "LOGICAL1", "LONG", "LONGBINARY",
            "LONGCHAR", "LONGTEXT", "LOWER",
            // M
            "MATCH", "MAX", "MEMO", "MIN", "MINUTE", "MODULE",/*"MONEY",*/"MONTH",/*"NAMES",*/"NATIONAL", "NATURAL", "NCHAR", "NEXT",
            // N
            "NO", "NOT", "NOTE", "NULL", "NULLIF", "NUMBER", "NUMERIC",
            // O
            "OBJECT", "OCTET_LENGTH", "OF", "OLEOBJECT", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "OUTER", "OUTPUT",
            "OVERLAPS", "OWNERACCESS",
            // P
            "PAD", "PATH", "PARAMETERS", "PARTIAL", "PASSWORD", "PERCENT", "PIVOT", "POSITION", "PRECISION", "PREPARE",
            "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROC", "PROCEDURE", "PUBLIC",
            // R
            "READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", "ROLLBACK", "ROWS",
            // S
            "SCHEMA", "SCROLL", "SECOND", "SECTION", "SELECT", "SELECTSCHEMA", "SELECTSECURITY", "SESSION", "SESSION_USER",
            "SET", "SHORT", "SINGLE", "SIZE", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE",
            "STRING", "SUBSTRING", "SUM", "SYSTEM_USER", "SYSDATE", "SYSTIMESTAMP",
            // T
            "TABLE", "TABLEID", "TEMPORARY", "TEXT", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE",
            "TO", "TOP", "TRAILING", "TRANSACTION", "TRANSFORM", "TRANSLATE", "TRANSLATION", "TRIM", "TRUE",
            // U
            "USER",
            //V
            "VALUE",
            // others
            "ALTERCOLUMN", "ALTERTABLE", "ADDCONSTRAINT", "BACKUPDATABASE", "CREATEDATABASE", "CREATEINDEX",
            "CREATEORREPLACEVIEW", "CREATETABLE", "CREATEPROCEDURE", "CREATEUNIQUEINDEX", "CREATEVIEW", "DROPCOLUMN",
            "DROPCONSTRAINT", "DROPDATABASE", "DROPDEFAULT", "DROPINDEX", "DROPTABLE", "DROPVIEW", "FOREIGNKEY",
            "FULLOUTERJOIN", "GROUPBY", "INNERJOIN", "INSERTINTO", "INSERTINTOSELECT", "ISNULL", "ISNOTNULL",
            "LEFTJOIN", "LIMIT", "NOTNULL", "ORDERBY", "OUTERJOIN", "PRIMARYKEY", "RIGHTJOIN", "ROWNUM", "SELECTDISTINCT",
            "SELECTINTO", "SELECTTOP", "TRUNCATETABLE", "UNION", "UNIONALL", "UNIQUE", "UPDATE", "VALUES", "VIEW", "WHERE"));


    protected String getSchemaTableSql(Table table) {
        if (table.getSchema() != null && !table.getSchema().isEmpty()) {
            return table.getSchema() + "." + table.getTableName();
        } else {
            return table.getTableName();
        }
    }


    protected String getSchemaIndexSql(Table table, String indexName) {
        if (table.getSchema() != null && !table.getSchema().isEmpty()) {
            return table.getSchema() + "." + indexName;
        } else {
            return indexName;
        }
    }

    /**
     * 表名在元数据表中是大写存储吗？
     */
    protected Boolean isTableNameUpperCase() {
        return false;
    }

    /**
     * 元数据中的表名，没有逃逸字符包裹时全小写或者全大写存储。
     */
    @Override
    public String getTableNameInMeta(Table table) {
        if (table.getTableName().charAt(0) == getEscapeCharacter()) {
            return unWrapper(table.getTableName());
        }
        if (isTableNameUpperCase() == null) {
            return table.getTableName();
        } else if (isTableNameUpperCase()) {
            return table.getTableName().toUpperCase();
        } else {
            return table.getTableName().toLowerCase();
        }
    }

    @Override
    public String getSchemaNameInMeta(Table table) {
        if (StringUtil.hasText(table.getSchema())) {
            if (isTableNameUpperCase() == null) {
                return table.getSchema();
            } else if (isTableNameUpperCase()) {
                return table.getSchema().toUpperCase();
            } else {
                return table.getSchema().toLowerCase();
            }
        }
        return null;
    }


    @Override
    public List<Sql> getCreateTableSql(Table table) {
        List<Sql> sqlList = new ArrayList<>();

        String sql = "CREATE TABLE " + getSchemaTableSql(table) + " (";

        List<String> columnSqlList = new ArrayList<>();
        for (Column column : table.getColumns()) {
            if (column.getAlterOrDropStrategy() != AlterOrDropStrategy.DROP) {
                String columnSql = column.getColumnName() + " " + getDataTypeDefinition(column) + getDefaultValueAndAdditionalDDlSql(table, column);
                columnSqlList.add(columnSql);
            }
        }
        sql += String.join(", ", columnSqlList);

        String pkColumnName = getPkColumnName(table);
        if (pkColumnName != null && !pkColumnName.isEmpty()) {
            sql += ", PRIMARY KEY ( " + pkColumnName + " )";
        }

        sql += ")";

        sqlList.add(new Sql(sql));

        if (table.getComment() != null) {
            sqlList.add(getSetTableCommentSql(table));
        }

        table.getColumns().stream().filter(c -> c.getAlterOrDropStrategy() != AlterOrDropStrategy.DROP)
                .filter(c -> c.getComment() != null).forEach(c -> sqlList.add(getSetColumnCommentSql(table, c)));

        return sqlList;
    }

    protected String getPkColumnName(Table table) {
        List<String> pkColumnNameList = table.getPrimaryKeyColumns();
        if (pkColumnNameList == null || pkColumnNameList.isEmpty()) return null;
        for (String column : pkColumnNameList) {
            if (table.getColumns().stream().noneMatch(c -> c.getColumnName().equalsIgnoreCase(column.trim()))) {
                return null;
            }
        }
        return String.join(", ", pkColumnNameList);
    }

    @Override
    public Sql getDropTableSql(Table table) {
        String sql = "DROP TABLE IF EXISTS " + getSchemaTableSql(table);
        return new Sql(sql);
    }

    @Override
    public Sql getRenameTableSql(Table oldTable, Table newTable) {
        String sql = "ALTER TABLE ";
        if (oldTable.getSchema() != null && !oldTable.getSchema().isEmpty()) {
            sql += oldTable.getSchema() + ".";
        }
        sql += oldTable.getTableName();
        sql += " RENAME TO ";
        if (newTable.getSchema() != null && !newTable.getSchema().isEmpty()) {
            sql += newTable.getSchema() + ".";
        }
        sql += newTable.getTableName();

        return new Sql(sql);
    }

    @Override
    public List<Sql> getAddColumnSql(Table table, Column column) {
        String sql = "ALTER TABLE " + getSchemaTableSql(table) + " ADD ";
        sql += column.getColumnName();
        sql += " ";
        sql += getDataTypeDefinition(column);
        sql += getDefaultValueAndAdditionalDDlSql(table, column);
        Sql sql1 = new Sql(sql);

        if (column.getComment() != null) {
            return Arrays.asList(sql1, getSetColumnCommentSql(table, column));
        } else {
            return Collections.singletonList(sql1);
        }
    }

    @Override
    public Sql getDropColumnSql(Table table, Column column) {
        String sql = "ALTER TABLE " + getSchemaTableSql(table) + " DROP " + column.getColumnName();
        return new Sql(sql);
    }

    @Override
    public List<Sql> getAlterColumnTypeSql(Table table, Column column, Column oldColumn) {
        List<Sql> sqlList = new ArrayList<>();

        if (oldColumn != null && !oldColumn.getColumnName().equals(column.getColumnName())) {
            String sql = "ALTER TABLE " + getSchemaTableSql(table) + " RENAME COLUMN " + oldColumn.getColumnName() + " TO " + column.getColumnName();
            sqlList.add(new Sql(sql));
        }
        if (oldColumn == null || !oldColumn.getDataTypeDefinition().equals(column.getDataTypeDefinition()) || !Objects.equals(getDefaultValueAndAdditionalDDlSql(table, column), getDefaultValueAndAdditionalDDlSql(table, oldColumn))) {
            // ALTER TABLE table_name ALTER COLUMN column_name TYPE new_dat
            String sql = "ALTER TABLE " + getSchemaTableSql(table) + " MODIFY ";
            sql += column.getColumnName();
            sql += " ";
            sql += getDataTypeDefinition(column);
            sql += getDefaultValueAndAdditionalDDlSql(table, column);
            sqlList.add(new Sql(sql));
        }
        return sqlList;
    }

    protected String getDefaultValueAndAdditionalDDlSql(Table table, Column column) {
        String sql = "";

        if (column.getNotNull() == Boolean.TRUE) {
            sql += " NOT NULL";
        }

        if (table.getKeyGeneratorMode() == KeyGeneratorMode.AUTO_INCREMENT && column.getColumnName().equals(table.getKeyGeneratorColumn())) {
            sql += " " + getAutoIncrementSql();
        } else if (StringUtil.hasText(column.getDefaultValue())) {
            sql += " DEFAULT ";
            sql += column.getDefaultValue();
        }
        if (column.getAdditionalDDl() != null && !column.getAdditionalDDl().isEmpty()) {
            sql += " " + column.getAdditionalDDl();
        }
        return sql;
    }

    protected String getAutoIncrementSql() {
        //  id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1 INCREMENT BY 1),
        return "GENERATED BY DEFAULT AS IDENTITY";
    }

    @Override
    public Sql getAddIndexSql(Table table, Column column, @NonNull String indexName) {
        String sql = "CREATE";
        if (column.getIndexType() != null && column.getIndexType() != IndexType.NORMAL) {
            sql += " " + column.getIndexType();
        }

        sql += " INDEX ";
        sql += indexName;
        sql += " ON ";
        sql += getSchemaTableSql(table);
        sql += " ( ";

        if (StringUtil.hasText(column.getCustomIndexColumn())) {
            sql += column.getCustomIndexColumn();
        } else {
            sql += column.getColumnName();
        }
        sql += " )";

        return new Sql(sql);
    }

    @Override
    public Sql getSetTableCommentSql(Table table) {
        //COMMENT ON TABLE table_name IS '新的表注释';
        String sql = "COMMENT ON TABLE " + getSchemaTableSql(table) + " IS '" + table.getComment() + "'";
        return new Sql(sql, true);
    }

    @Override
    public Sql getSetColumnCommentSql(Table table, Column column) {
        //COMMENT ON COLUMN test_table.id IS '这是主键';
        String sql = "COMMENT ON COLUMN " + getSchemaTableSql(table) + "." + column.getColumnName() + " IS '" + column.getComment() + "'";
        return new Sql(sql, true);
    }

    public String normalizeColumnType(String columnType, Integer characterMaximumLength) {
        columnType = columnType.toUpperCase();
        if (columnType.equals("INT4")) {
            return "NUMERIC";
        }
        if (columnType.equals("DECIMAL")) {
            return "INTEGER";
        }
        if (columnType.startsWith("TIMESTAMP")) {
            return "TIMESTAMP";
        }
        if (columnType.startsWith("VECTOR")) {
            return "VECTOR";
        }
        // 解决H2、PG数据库执行时出现 CHARACTER VARYING 类型问题
        if ("CHARACTER VARYING".equals(columnType)) {
            if (characterMaximumLength != null && characterMaximumLength == 1000000000L) {
                return "TEXT";
            } else {
                return "VARCHAR";
            }
        }
        return columnType;
    }

    @Override
    public void normalizeColumn(Column column) {
        String columnType = normalizeColumnType(column.getDataType(), column.getCharacterMaximumLength());
        column.setDataType(columnType);

        //处理 vector 长度
        if ("VECTOR".equals(columnType)) {
            if (column.getDataType().contains("(")) {
                column.setVectorLength(Integer.parseInt(column.getDataType().substring("VECTOR".length() + 1, column.getDataType().length() - 1)));
            }
        }

        //清理 numericScale
        if ("NUMERIC".equals(columnType) || "DECIMAL".equals(columnType)) {
            if (column.getNumericScale() == null) {
                column.setNumericScale(0);
            }
        } else {
            column.setNumericScale(null);
        }

        //清理 characterMaximumLength
        if (columnType.endsWith("CHAR")) {
            //超过mysql的最大长度时清空长度设置
            if (column.getCharacterMaximumLength() != null && column.getCharacterMaximumLength() > 65535) {
                column.setCharacterMaximumLength(null);
            }
        } else {
            // 达梦数据库返回列是数字类型，也有字符串长度
            column.setCharacterMaximumLength(null);
        }
    }


    protected String getDataTypeDefinition(Column c) {
        return c.getDataTypeDefinition();
    }

    /**
     * escape 字符
     */
    protected abstract char getEscapeCharacter();

    /**
     * 存在中文等非法字符，或者为数据库关键字时进行包装处理
     */
    @Override
    public String wrapper(String name) {
        if (name == null) {
            return null;
        }
        String w = String.valueOf(getEscapeCharacter());
        if (name.startsWith(w) && name.endsWith(w)) {
            return name;
        }
        if (!name.matches("[a-zA-Z_][a-zA-Z0-9_]*") || KEYWORD.contains(name.toUpperCase())) {
            return w + name + w;
        }
        return name;
    }

    @Override
    public String unWrapper(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        char w = getEscapeCharacter();
        if (name.charAt(0) == w && name.charAt(name.length() - 1) == w) {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }
}
