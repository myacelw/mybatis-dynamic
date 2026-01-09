package io.github.myacelw.mybatis.dynamic.core.database.dialect;

import io.github.myacelw.mybatis.dynamic.core.metadata.enums.ColumnAlterStrategy;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.Sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Oracle数据库方言
 *
 * @author liuwei
 */
public class OracleDataBaseDialect extends AbstractDataBaseDialect {


    @Override
    public String getName() {
        return "oracle";
    }

    @Override
    protected char getEscapeCharacter() {
        return '"';
    }

    @Override
    protected Boolean isTableNameUpperCase() {
        return true;
    }

    @Override
    public boolean supportAutoIncrement() {
        return false;
    }

    @Override
    public boolean supportSequence() {
        return true;
    }

    @Override
    public List<Sql> getCreateTableSql(Table table) {
        List<Sql> sqlList = new ArrayList<>();

        String sql = "CREATE TABLE " + getSchemaTableSql(table) + " (";

        List<String> columnSqlList = new ArrayList<>();
        for (Column column : table.getColumns()) {
            if (column.getAlterOrDropStrategy() != ColumnAlterStrategy.DROP) {
                String columnSql = column.getColumnName() + " " + getDataTypeDefinition(column) + getDefaultValueAndAdditionalDDlSql(table, column);
                columnSqlList.add(columnSql);
            }
        }
        sql += String.join(", ", columnSqlList);
        sql += ")";


        sqlList.add(new Sql(sql));
        sqlList.add(getCreateTablePkSql(table));

        if (table.getComment() != null) {
            sqlList.add(getSetTableCommentSql(table));
        }

        table.getColumns().stream().filter(c -> c.getAlterOrDropStrategy() != ColumnAlterStrategy.DROP)
                .filter(c -> c.getComment() != null).forEach(c -> sqlList.add(getSetColumnCommentSql(table, c)));
        return sqlList;
    }


    @Override
    public Sql getDropIndexSql(Table table, String indexName) {
        String sql = "DROP INDEX " + getSchemaIndexSql(table, indexName);
        return new Sql(sql, true);
    }

    private Sql getCreateTablePkSql(Table table) {
        String tablePkConstraintName = getTablePkConstraintName(table.getTableName());
        String pkColumnName = getPkColumnName(table);
        String sql = "ALTER TABLE " + getSchemaTableSql(table) + " ADD CONSTRAINT " + tablePkConstraintName + " PRIMARY KEY ( " + pkColumnName + " )";
        return new Sql(sql);
    }

    private String getTablePkConstraintName(String tableName) {
        if (tableName.endsWith("\"")) {
            return "\"PK_" + tableName.substring(1, tableName.length() - 1) + "\"";
        }
        return "PK_" + tableName;
    }

    @Override
    public String getListAggFunctionSql(String columnName) {
        return "LISTAGG(" + columnName + " , ',')";
    }

    @Override
    public String getListAggDistinctFunctionSql(String columnName) {
        return "LISTAGG(DISTINCT " + columnName + " , ',')";
    }

    @Override
    public String getJsonArrayAggFunctionSql(String columnName) {
        return "JSONB_AGG(" + columnName + ")";
    }

    @Override
    public String getJsonArrayAggDistinctFunctionSql(String columnName) {
        return "JSONB_AGG(DISTINCT " + columnName + ")";
    }

}
