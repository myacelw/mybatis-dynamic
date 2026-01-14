package io.github.myacelw.mybatis.dynamic.core.database.impl;

import io.github.myacelw.mybatis.dynamic.core.Database;
import io.github.myacelw.mybatis.dynamic.core.TableServiceBuildUtil;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Index;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataBaseMetaDataHelperImplTest {

    static DataBaseMetaDataHelperImpl metaDataHelper;
    static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    static void setup() throws Exception {
        sqlSessionFactory = TableServiceBuildUtil.createSqlSessionFactory(Database.H2, "DataBaseMetaDataHelperImplTest");
        metaDataHelper = new DataBaseMetaDataHelperImpl(sqlSessionFactory);

        try (Connection connection = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE test_table (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL, description TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("COMMENT ON TABLE test_table IS 'Test table comment'");
            statement.execute("COMMENT ON COLUMN test_table.name IS 'Name column comment'");
            statement.execute("CREATE INDEX idx_test_name ON test_table(name)");
            statement.execute("CREATE UNIQUE INDEX uk_test_id_name ON test_table(id, name)");
        }
    }

    @Test
    void getDatabaseProductName() {
        String productName = metaDataHelper.getDatabaseProductName();
        assertEquals("H2", productName);
    }

    @Test
    void getTable_ExistingTable() {

        String tableName = metaDataHelper.getWrappedIdentifierInMeta("test_table");

        Table table = metaDataHelper.getTable(tableName, null);
        assertNotNull(table);
        assertEquals(tableName, table.getTableName());
        assertEquals("Test table comment", table.getComment());
    }

    @Test
    void getTable_NonExistingTable() {
        String tableName = metaDataHelper.getWrappedIdentifierInMeta("non_existing_table");
        Table table = metaDataHelper.getTable(tableName, null);
        assertNull(table);
    }

    @Test
    void getColumns() {
        String tableName = metaDataHelper.getWrappedIdentifierInMeta("test_table");
        List<Column> columns = metaDataHelper.getColumns(tableName, null);
        assertNotNull(columns);
        assertEquals(4, columns.size());

        Column idColumn = columns.stream().filter(c -> "id".equalsIgnoreCase(c.getColumnName())).findFirst().orElse(null);
        assertNotNull(idColumn);
        assertTrue(idColumn.getNotNull());
        assertTrue(idColumn.isAutoIncrement());

        Column nameColumn = columns.stream().filter(c -> "name".equalsIgnoreCase(c.getColumnName())).findFirst().orElse(null);
        assertNotNull(nameColumn);
        assertTrue("VARCHAR".equalsIgnoreCase(nameColumn.getDataType()) || "CHARACTER VARYING".equalsIgnoreCase(nameColumn.getDataType()));
        assertEquals(255, nameColumn.getCharacterMaximumLength());
        assertTrue(nameColumn.getNotNull());
        assertEquals("Name column comment", nameColumn.getComment());

        Column descriptionColumn = columns.stream().filter(c -> "description".equalsIgnoreCase(c.getColumnName())).findFirst().orElse(null);
        assertNotNull(descriptionColumn);
        assertFalse(descriptionColumn.getNotNull());

        Column createdAtColumn = columns.stream().filter(c -> "created_at".equalsIgnoreCase(c.getColumnName())).findFirst().orElse(null);
        assertNotNull(createdAtColumn);
        assertNotNull(createdAtColumn.getDefaultValue());
    }

    @Test
    void getIndexList() {
        String tableName = metaDataHelper.getWrappedIdentifierInMeta("test_table");
        List<Index> indexList = metaDataHelper.getIndexList(tableName, null);
        assertNotNull(indexList);
        // H2 might report the primary key as an index as well.
        // uk_test_id_name, idx_test_name, and potentially PRIMARY_KEY_...
        assertTrue(indexList.size() >= 2);

        Index ukIndex = indexList.stream().filter(i -> "uk_test_id_name".equalsIgnoreCase(i.getIndexName())).findFirst().orElse(null);
        assertNotNull(ukIndex);
        assertEquals(io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType.UNIQUE, ukIndex.getIndexType());
        assertEquals(2, ukIndex.getColumnNames().size());
        assertTrue(ukIndex.getColumnNames().contains(metaDataHelper.getWrappedIdentifierInMeta("id")));
        assertTrue(ukIndex.getColumnNames().contains(metaDataHelper.getWrappedIdentifierInMeta("name")));

        Index idxIndex = indexList.stream().filter(i -> "idx_test_name".equalsIgnoreCase(i.getIndexName())).findFirst().orElse(null);
        assertNotNull(idxIndex);
        assertEquals(io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType.NORMAL, idxIndex.getIndexType());
        assertEquals(1, idxIndex.getColumnNames().size());
        assertEquals(metaDataHelper.getWrappedIdentifierInMeta("name"), idxIndex.getColumnNames().get(0));
    }
}
