package io.github.myacelw.mybatis.dynamic.core.database.impl;

import io.github.myacelw.mybatis.dynamic.core.database.DataBaseMetaDataHelper;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Index;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据库元数据帮助类
 *
 * @author liuwei
 */
@Slf4j
public class DataBaseMetaDataHelperImpl implements DataBaseMetaDataHelper {

    private static final Set<String> STANDARD_KEYWORDS = new HashSet<>(Arrays.asList(
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

    private static final Map<DataSource, MetaDataInfo> CACHE = new ConcurrentHashMap<>();

    final SqlSessionFactory sqlSessionFactory;

    public DataBaseMetaDataHelperImpl(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Value
    @Builder
    private static class MetaDataInfo {
        String identifierQuoteString;
        Set<String> keywords;
        // Case sensitivity for unquoted identifiers
        boolean supportsMixedCaseIdentifiers;
        boolean storesUpperCaseIdentifiers;
        boolean storesLowerCaseIdentifiers;
        boolean storesMixedCaseIdentifiers;
        // Case sensitivity for quoted identifiers
        boolean supportsMixedCaseQuotedIdentifiers;
        boolean storesUpperCaseQuotedIdentifiers;
        boolean storesLowerCaseQuotedIdentifiers;
        boolean storesMixedCaseQuotedIdentifiers;
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
    public String getIdentifierQuoteString() {
        return getMetaDataInfo().getIdentifierQuoteString();
    }

    private MetaDataInfo getMetaDataInfo() throws SQLException {
        DataSource dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        MetaDataInfo info = CACHE.get(dataSource);
        if (info != null) {
            return info;
        }

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String quoteString = metaData.getIdentifierQuoteString();
            if (" ".equals(quoteString)) {
                quoteString = "";
            }

            Set<String> keywords = new HashSet<>(STANDARD_KEYWORDS);
            String extraKeywords = metaData.getSQLKeywords();
            if (extraKeywords != null && !extraKeywords.isEmpty()) {
                Arrays.stream(extraKeywords.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                        .forEach(s -> keywords.add(s.toUpperCase()));
            }
            // Add functions as keywords to be safe
            addFunctions(metaData.getNumericFunctions(), keywords);
            addFunctions(metaData.getStringFunctions(), keywords);
            addFunctions(metaData.getSystemFunctions(), keywords);
            addFunctions(metaData.getTimeDateFunctions(), keywords);

            info = MetaDataInfo.builder()
                    .identifierQuoteString(quoteString)
                    .keywords(keywords)
                    .supportsMixedCaseIdentifiers(metaData.supportsMixedCaseIdentifiers())
                    .storesUpperCaseIdentifiers(metaData.storesUpperCaseIdentifiers())
                    .storesLowerCaseIdentifiers(metaData.storesLowerCaseIdentifiers())
                    .storesMixedCaseIdentifiers(metaData.storesMixedCaseIdentifiers())
                    .supportsMixedCaseQuotedIdentifiers(metaData.supportsMixedCaseQuotedIdentifiers())
                    .storesUpperCaseQuotedIdentifiers(metaData.storesUpperCaseQuotedIdentifiers())
                    .storesLowerCaseQuotedIdentifiers(metaData.storesLowerCaseQuotedIdentifiers())
                    .storesMixedCaseQuotedIdentifiers(metaData.storesMixedCaseQuotedIdentifiers())
                    .build();

            CACHE.put(dataSource, info);
            return info;
        }
    }

    private void addFunctions(String functions, Set<String> keywords) {
        if (functions != null && !functions.isEmpty()) {
            Arrays.stream(functions.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                    .forEach(s -> keywords.add(s.toUpperCase()));
        }
    }

    @SneakyThrows
    @Override
    public boolean isIdentifierReserved(String identifier) {
        if (identifier == null) {
            return false;
        }
        return getMetaDataInfo().getKeywords().contains(identifier.toUpperCase());
    }

    @SneakyThrows
    @Override
    public String wrapIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        String quote = getIdentifierQuoteString();
        if (quote == null || quote.isEmpty()) {
            return identifier;
        }
        if (identifier.startsWith(quote) && identifier.endsWith(quote)) {
            return identifier;
        }
        if (!identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*") || isIdentifierReserved(identifier)) {
            return quote + identifier + quote;
        }
        return identifier;
    }

    @SneakyThrows
    @Override
    public String unwrapIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        String quote = getIdentifierQuoteString();
        if (quote == null || quote.isEmpty()) {
            return identifier;
        }
        if (identifier.startsWith(quote) && identifier.endsWith(quote)) {
            return identifier.substring(quote.length(), identifier.length() - quote.length());
        }
        return identifier;
    }

    @SneakyThrows
    @Override
    public String getIdentifierInMeta(String identifier, boolean isQuoted) {
        if (identifier == null) {
            return null;
        }
        String quote = getIdentifierQuoteString();
        boolean actuallyQuoted = isQuoted;
        String name = identifier;
        if (quote != null && !quote.isEmpty() && identifier.startsWith(quote) && identifier.endsWith(quote)) {
            actuallyQuoted = true;
            name = identifier.substring(quote.length(), identifier.length() - quote.length());
        }

        MetaDataInfo info = getMetaDataInfo();
        if (actuallyQuoted) {
            if (info.isStoresUpperCaseQuotedIdentifiers()) {
                return name.toUpperCase();
            } else if (info.isStoresLowerCaseQuotedIdentifiers()) {
                return name.toLowerCase();
            }
        } else {
            if (info.isStoresUpperCaseIdentifiers()) {
                return name.toUpperCase();
            } else if (info.isStoresLowerCaseIdentifiers()) {
                return name.toLowerCase();
            }
        }
        return name;
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
