package io.github.myacelw.mybatis.dynamic.core.database.dialect;

import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * H2数据库方言
 *
 * @author liuwei
 */
public class H2DataBaseDialect extends AbstractDataBaseDialect {

    @Override
    public String getName() {
        return "h2";
    }

    @Override
    public boolean supportAutoIncrement() {
        return true;
    }

    @Override
    public boolean supportSequence() {
        return true;
    }

    @Override
    public Sql getDropIndexSql(Table table, String indexName) {
        String sql = "DROP INDEX " + getSchemaIndexSql(table, indexName) + " ON " + getSchemaTableSql(table);
        return new Sql(sql);
    }

    @Override
    public List<Sql> getAlterColumnTypeSql(Table table, Column column, Column oldColumn) {
        List<Sql> sqlList = new ArrayList<>();

        if (oldColumn != null && !oldColumn.getColumnName().equals(column.getColumnName())) {
            String sql = "ALTER TABLE " + getSchemaTableSql(table) + " ALTER COLUMN " + oldColumn.getColumnName() + " RENAME TO " + column.getColumnName();
            sqlList.add(new Sql(sql));
        }
        if (oldColumn == null || !oldColumn.getDataTypeDefinition().equals(column.getDataTypeDefinition()) || !Objects.equals(getDefaultValueAndAdditionalDDlSql(table, column), getDefaultValueAndAdditionalDDlSql(table, oldColumn))) {
            String sql = "ALTER TABLE " + getSchemaTableSql(table) + " ALTER COLUMN ";
            sql += column.getColumnName();
            sql += " ";
            sql += getDataTypeDefinition(column);
            sql += getDefaultValueAndAdditionalDDlSql(table, column);
            sqlList.add(new Sql(sql));
        }
        return sqlList;
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

    public int getPriority() {
        return 100;
    }

}
