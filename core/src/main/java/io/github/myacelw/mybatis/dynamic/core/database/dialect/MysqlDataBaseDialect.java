package io.github.myacelw.mybatis.dynamic.core.database.dialect;

import io.github.myacelw.mybatis.dynamic.core.metadata.enums.ColumnAlterStrategy;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.Sql;
import lombok.NonNull;

import java.util.*;

/**
 * Mysql数据库方言
 *
 * @author liuwei
 */
public class MysqlDataBaseDialect extends AbstractDataBaseDialect {

    @Override
    public boolean supportAutoIncrement() {
        return true;
    }

    @Override
    public boolean supportSequence() {
        return false;
    }

    @Override
    public String getName() {
        return "mysql";
    }

    @Override
    protected char getEscapeCharacter() {
        return '`';
    }

    @Override
    public boolean isAlertColumnIncludeComment() {
        return true;
    }

    @Override
    public List<Sql> getCreateTableSql(Table table) {
        String sql = "CREATE TABLE " + getSchemaTableSql(table) + " (";

        List<String> columnSqlList = new ArrayList<>();
        for (Column column : table.getColumns()) {
            if (column.getAlterOrDropStrategy() != ColumnAlterStrategy.DROP) {
                columnSqlList.add(doColumnSql(table, column));
            }
        }
        String pkColumnName = getPkColumnName(table);
        if (pkColumnName != null && !pkColumnName.isEmpty()) {
            columnSqlList.add("PRIMARY KEY (" + pkColumnName + ")");
        }
        sql += String.join(", ", columnSqlList);
        sql += ")";

        if (table.getComment() != null) {
            sql += " COMMENT '" + table.getComment() + "'";
        }
        sql += " " + getCreateTableSqlOthers(table);
        sql += " " + getPartitionDefinition(table);

        return Collections.singletonList(new Sql(sql));
    }

    @Override
    protected String getAutoIncrementSql() {
        return "AUTO_INCREMENT";
    }

    protected String getCreateTableSqlOthers(Table table) {
        return "";
    }

    protected String getPartitionDefinition(Table table) {
        if (table.getPartition() != null) {
            return "\n" + table.getPartition().getSql(true);
        }
        return "";
    }

    @Override
    public List<Sql> getAddColumnSql(Table table, Column column) {
        String sql = "ALTER TABLE " + getSchemaTableSql(table) + " ADD " + doColumnSql(table, column);
        return Collections.singletonList(new Sql(sql));
    }

    @Override
    public List<Sql> getAlterColumnTypeSql(Table table, Column column, Column oldColumn) {
        String oldColumnName = oldColumn != null ? oldColumn.getColumnName() : column.getColumnName();
        // ALTER TABLE table_name CHANGE COLUMN old_name new_name new_data_type [constraints];
        String sql = "ALTER TABLE " + getSchemaTableSql(table) + " CHANGE COLUMN " + oldColumnName + " " + doColumnSql(table, column);
        return Collections.singletonList(new Sql(sql));
    }

    protected String doColumnSql(Table table, Column column) {
        String sql = column.getColumnName() + " "
                + getDataTypeDefinition(column)
                + getDefaultValueAndAdditionalDDlSql(table, column);

        if (column.getComment() != null) {
            sql += " COMMENT '" + column.getComment() + "'";
        }
        return sql;
    }

    @Override
    public Sql getAddIndexSql(Table table, Column column, @NonNull String indexName) {
        Sql sql = super.getAddIndexSql(table, column, indexName);
        if (column.getIndexType() == IndexType.FULLTEXT) {
            return new Sql(sql.getSql() + " WITH PARSER ngram", true);
        }
        return sql;
    }

    @Override
    public Sql getSetTableCommentSql(Table table) {
        String sql = "ALTER TABLE " + getSchemaTableSql(table) + " COMMENT = '" + table.getComment() + "'";
        return new Sql(sql, true);
    }

    @Override
    public Sql getSetColumnCommentSql(Table table, Column column) {
        return getAlterColumnTypeSql(table, column, null).get(0);
    }

    @Override
    public Sql getDropIndexSql(Table table, String indexName) {
        String sql = "DROP INDEX " + indexName + " ON " + getSchemaTableSql(table);
        return new Sql(sql, true);
    }

    /**
     * Mysql要求分区键中的列必须为主键列
     */
    @Override
    protected String getPkColumnName(Table table) {
        Set<String> pkSet = new LinkedHashSet<>();
        if (table.getPartition() != null) {
            pkSet.addAll(table.getPartition().getFields());
        }
        String pkColumnName = super.getPkColumnName(table);
        if (pkColumnName != null) {
            pkSet.addAll(Arrays.asList(pkColumnName.split(",")));
        }
        if (pkSet.isEmpty()) {
            return null;
        }
        return String.join(",", pkSet);
    }

    @Override
    public String getListAggFunctionSql(String columnName) {
        return "GROUP_CONCAT(" + columnName + ")";
    }

    @Override
    public String getListAggDistinctFunctionSql(String columnName) {
        return "GROUP_CONCAT(DISTINCT " + columnName + ")";
    }

    @Override
    public String getJsonArrayAggFunctionSql(String columnName) {
        return "JSON_ARRAYAGG(" + columnName + ")";
    }

    @Override
    public String getJsonArrayAggDistinctFunctionSql(String columnName) {
        return "JSON_ARRAYAGG(DISTINCT " + columnName + ")";
    }
}
