package io.github.myacelw.mybatis.dynamic.core.database.dialect;

import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.Sql;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Postgresql数据库方言
 *
 * @author liuwei
 */
@Getter
public class PostgresqlDataBaseDialect extends AbstractDataBaseDialect {

    public PostgresqlDataBaseDialect() {
    }

    @Override
    public String getName() {
        return "postgresql";
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
    public Sql getRenameTableSql(Table oldTable, Table newTable) {
        String sql = "ALTER TABLE ";
        if (newTable.getSchema() != null && !newTable.getSchema().isEmpty()) {
            sql += newTable.getSchema() + ".";
        }
        sql += oldTable.getTableName();
        sql += " RENAME TO ";
        sql += newTable.getTableName();
        return new Sql(sql);
    }

    @Override
    public Sql getDropIndexSql(Table table, String indexName) {
        String sql = "DROP INDEX " + getSchemaIndexSql(table, indexName);
        return new Sql(sql, true);
    }

    @Override
    public List<Sql> getAlterColumnTypeSql(Table table, Column column, Column oldColumn) {
        List<Sql> sqlList = new ArrayList<>();

        String sql = "ALTER TABLE " + getSchemaTableSql(table);
        boolean split = false;
        // 处理列名变更
        if (oldColumn != null && !oldColumn.getColumnName().equals(column.getColumnName())) {
            sql += " RENAME COLUMN " + oldColumn.getColumnName() + " TO " + column.getColumnName();
            split = true;
        }

        // 处理数据类型变更
        if (oldColumn == null || !oldColumn.getDataTypeDefinition().equals(column.getDataTypeDefinition())) {
            if (split) {
                sql += ", ";
            }
            sql += "ALTER COLUMN "; // ${columnName} TYPE ${dataType} USING ${columnName}::${dataType}";
            sql += column.getColumnName();
            sql += " TYPE ";
            sql += getDataTypeDefinition(column);
            sql += " USING ";
            sql += column.getColumnName();
            sql += "::";
            sql += getDataTypeDefinition(column);

            split = true;
        }

        // 处理默认值变更
        if (oldColumn == null || !Objects.equals(oldColumn.getDefaultValue(), column.getDefaultValue())) {
            if (split) {
                sql += ", ";
            }
            sql += "ALTER COLUMN ";
            sql += column.getColumnName();

            if (StringUtil.hasText(column.getDefaultValue())) {
                sql += " SET DEFAULT ";
                sql += column.getDefaultValue();
            } else {
                sql += " DROP DEFAULT";
            }

            split = true;
        }

        sqlList.add(new Sql(sql));
        return sqlList;
    }

    @Override
    public String getListAggFunctionSql(String columnName) {
        return "STRING_AGG(" + columnName + " , ',')";
    }

    @Override
    public String getListAggDistinctFunctionSql(String columnName) {
        return "STRING_AGG(DISTINCT " + columnName + " , ',')";
    }

    @Override
    public String getJsonArrayAggFunctionSql(String columnName) {
        return "JSON_ARRAYAGG(" + columnName + ")";
    }

    @Override
    public String getJsonArrayAggDistinctFunctionSql(String columnName) {
        return "JSON_ARRAYAGG(DISTINCT " + columnName + ")";
    }

    @Override
    public Sql getAddIndexSql(Table table, Column column, @NonNull String indexName) {
        if ("VECTOR".equalsIgnoreCase(column.getDataType())) {
            return new Sql("CREATE INDEX " + indexName + " ON " + getSchemaTableSql(table) + " USING hnsw (" + column.getColumnName() + " vector_l2_ops) WITH ( m = 16, ef_construction = 64, ef_search = 10 )");
        } else if (column.getIndexType() == IndexType.FULLTEXT) {
            return new Sql("CREATE INDEX " + indexName + " ON " + getSchemaTableSql(table) + " USING gin(to_tsvector('chinese', " + column.getColumnName() + " ))");
        }
        return super.getAddIndexSql(table, column, indexName);
    }

}
