package io.github.myacelw.mybatis.dynamic.core.database.impl;

import io.github.myacelw.mybatis.dynamic.core.database.DataBaseMetaDataHelper;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Index;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库元数据帮助类
 *
 * @author liuwei
 */
@Slf4j
public class DataBaseMetaDataHelperImpl implements DataBaseMetaDataHelper {

    final SqlSessionFactory sqlSessionFactory;

    public DataBaseMetaDataHelperImpl(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @SneakyThrows
    @Override
    public Table getTable(String tableName, String schema) {
        try (Connection connection = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection()) {
            CatalogAndSchema catalogAndSchema = getCatalogAndSchema(schema, connection);
            try (ResultSet resultSet = connection.getMetaData().getTables(catalogAndSchema.catalog, catalogAndSchema.schema, tableName, new String[]{"TABLE"})) {
                if (resultSet.next()) {
                    Table table = new Table(resultSet.getString("TABLE_NAME"), schema);
                    table.setComment(resultSet.getString("REMARKS"));
                    return table;
                }
            }
        }
        return null;
    }

    @SneakyThrows
    @Override
    public String getDatabaseProductName() {
        try (Connection connection = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getDatabaseProductName();
        }
    }

    @SneakyThrows
    @Override
    public List<Column> getColumns(String tableName, String schema) {
        try (Connection connection = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection()) {
            CatalogAndSchema catalogAndSchema = getCatalogAndSchema(schema, connection);
            List<Column> columns = new ArrayList<>();
            try (ResultSet resultSet = connection.getMetaData().getColumns(catalogAndSchema.catalog, catalogAndSchema.schema, tableName, null)) {
                while (resultSet.next()) {
                    Column column = new Column();
                    column.setColumnName(resultSet.getString("COLUMN_NAME"));
                    JDBCType jdbcType = JDBCType.valueOf(resultSet.getInt("DATA_TYPE"));
                    String typeName = resultSet.getString("TYPE_NAME");
                    column.setDataType(typeName);
                    column.setComment(resultSet.getString("REMARKS"));
                    column.setNotNull("NO".equals(resultSet.getString("IS_NULLABLE")));
                    column.setAutoIncrement("YES".equals(resultSet.getString("IS_AUTOINCREMENT")));
                    column.setGeneratedColumn("YES".equals(resultSet.getString("IS_GENERATEDCOLUMN")));
                    if (!column.isAutoIncrement() && !column.isGeneratedColumn()) {
                        column.setDefaultValue(resultSet.getString("COLUMN_DEF"));
                    }
                    if (jdbcType == JDBCType.CHAR || jdbcType == JDBCType.VARCHAR || jdbcType == JDBCType.LONGVARCHAR
                            || jdbcType == JDBCType.NCHAR || jdbcType == JDBCType.NVARCHAR) {
                        column.setCharacterMaximumLength(resultSet.getInt("COLUMN_SIZE"));
                    }

                    if (jdbcType == JDBCType.DECIMAL || jdbcType == JDBCType.NUMERIC || jdbcType == JDBCType.INTEGER || jdbcType == JDBCType.BIGINT || jdbcType == JDBCType.SMALLINT || jdbcType == JDBCType.TINYINT
                            || jdbcType == JDBCType.FLOAT || jdbcType == JDBCType.DOUBLE || jdbcType == JDBCType.REAL) {
                        column.setNumericPrecision(resultSet.getInt("COLUMN_SIZE"));
                        column.setNumericScale(resultSet.getInt("DECIMAL_DIGITS"));
                    }
                    columns.add(column);
                }
            }
            return columns;
        }
    }

    private static CatalogAndSchema getCatalogAndSchema(String schema, Connection connection) throws SQLException {
        String newCatalog = connection.getCatalog();
        String newSchema = connection.getSchema();
        if (schema != null) {
            if (newSchema != null) {
                newSchema = schema;
            } else {
                newCatalog = schema;
            }
        }
        return new CatalogAndSchema(newCatalog, newSchema);
    }


    @SneakyThrows
    @Override
    public List<Index> getIndexList(String tableName, String schema) {
        try (Connection connection = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection()) {
            CatalogAndSchema catalogAndSchema = getCatalogAndSchema(schema, connection);
            List<Index> indexList = new ArrayList<>();
            Map<String, List<ColumnNameAndOrdinalPosition>> indexColumnMap = new HashMap<>();
            try (ResultSet resultSet = connection.getMetaData().getIndexInfo(catalogAndSchema.catalog, catalogAndSchema.schema, tableName, false, true)) {
                while (resultSet.next()) {
                    String indexName = resultSet.getString("INDEX_NAME");
                    int ordinalPosition = resultSet.getInt("ORDINAL_POSITION");
                    String columnName = resultSet.getString("COLUMN_NAME");
                    if (indexName != null && columnName != null) {
                        ColumnNameAndOrdinalPosition columnNameAndOrdinalPosition = new ColumnNameAndOrdinalPosition(columnName, ordinalPosition);
                        indexColumnMap.computeIfAbsent(indexName, k -> new ArrayList<>()).add(columnNameAndOrdinalPosition);

                        Index index = new Index();
                        index.setIndexName(indexName);
                        if (!resultSet.getBoolean("NON_UNIQUE")) {
                            index.setIndexType(IndexType.UNIQUE);
                        } else {
                            index.setIndexType(IndexType.NORMAL);
                        }
                        indexList.add(index);
                    }
                }
                indexList.forEach(index -> index.setColumnNames(
                        indexColumnMap.get(index.getIndexName()).stream()
                                .sorted(Comparator.comparing(ColumnNameAndOrdinalPosition::getOrdinalPosition))
                                .map(ColumnNameAndOrdinalPosition::getColumnName)
                                .collect(Collectors.toList()))
                );
            }
            return indexList;
        }
    }

    @Value
    private static class ColumnNameAndOrdinalPosition {
        String columnName;
        int ordinalPosition;
    }

    @AllArgsConstructor
    private static class CatalogAndSchema {
        public final String catalog;
        public final String schema;
    }

}
