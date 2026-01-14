package io.github.myacelw.mybatis.dynamic.core.database.impl;

import io.github.myacelw.mybatis.dynamic.core.DataType;
import io.github.myacelw.mybatis.dynamic.core.Database;
import io.github.myacelw.mybatis.dynamic.core.TableServiceBuildUtil;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.typehandler.JsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.TypeHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class TableManagerImplTest {

    @ParameterizedTest
    @ValueSource(strings = {"H2"/*, "MYSQL", "POSTGRESQL", "OCEANBASE"*/})
    void createOrUpgradeTable(Database db) {
        TableManagerImpl tableService = TableServiceBuildUtil.createTableService(db);

        String tableName = "test_createOrUpgradeTable";

        Table table = new Table(tableName, null);

        try {
            tableService.dropTable(table);
        } catch (Exception e) {

        }
        DataType dataType = DataType.LONGTEXT;
        if (db == Database.ORACLE) {
            dataType = DataType.CLOB;
        }
        if (db == Database.POSTGRESQL || db == Database.H2) {
            dataType = DataType.TEXT;
        }

        List<Column> columns = new ArrayList<>();
        columns.add(c("id", DataType.VARCHAR.name(), 64, null, null, true, null));
        columns.add(c("name", DataType.VARCHAR.name(), 200, null, null, true, null));
        columns.add(c("age", DataType.NUMERIC.name(), null, 6, 0, true, null));
        columns.add(c("amount", DataType.NUMERIC.name(), null, 6, 4, true, null));
        columns.add(c("update_time", DataType.TIMESTAMP.name(), null, null, null, true, null));
        columns.add(c("text2", dataType.name(), null, null, null, false, JsonTypeHandler.class));
        columns.add(c("json2", dataType.name(), null, null, null, false, JsonTypeHandler.class));

        for (Column column : columns) {
            column.setColumnName(tableService.getMetaDataHelper().getWrappedIdentifierInMeta(column.getColumnName()));
        }

        //table.getColumns().add(c("Add1", DataType.VARCHAR.name(), 20, null, null, true, null));

        table.setColumns(columns);
        table.setPrimaryKeyColumns(Collections.singletonList("id"));


        tableService.dropTable(table);
        tableService.createOrUpgradeTable(table);
        assertColumnsEqual(tableService, tableName, null, table.getColumns(), db);
        //===============
        table.getColumns().add(c("Add1", DataType.VARCHAR.name(), 20, null, null, true, null));
        table.getColumns().add(c("add2", DataType.VARCHAR.name(), 20, null, null, true, null));
        table.getColumns().add(c("add3", DataType.VARCHAR.name(), 20, null, null, true, null));

        for (Column column : table.getColumns()) {
            column.setColumnName(tableService.getMetaDataHelper().getWrappedIdentifierInMeta(column.getColumnName()));
        }

        tableService.createOrUpgradeTable(table);
        assertColumnsEqual(tableService, table.getTableName(), null, table.getColumns(), db);
    }

    public static Column c(String columnName, String dataType, Integer characterMaximumLength, Integer numericPrecision, Integer numericScale, boolean index, Class<? extends TypeHandler> typeHandler) {
        Column column = new Column();
        column.setColumnName(columnName);
        column.setDataType(dataType);
        column.setCharacterMaximumLength(characterMaximumLength);
        column.setNumericPrecision(numericPrecision);
        column.setNumericScale(numericScale);
        column.setIndex(index);
        if (index) {
            column.setIndexName(columnName);
        }
        column.setTypeHandler(typeHandler);
        return column;
    }

    private void assertColumnsEqual(TableManagerImpl tableService, String tableName, String schema, List<Column> columns, Database db) {
        List<Column> actualList = tableService.getCurrentTableColumns(new Table(tableName, schema));
        Map<String, Column> actualMap = new HashMap<>();
        actualList.forEach(c -> actualMap.put(c.getColumnName(), c));
        assertEquals(columns.size(), actualList.size());
        for (Column c : columns) {
            Column actualColumn = actualList.stream().filter(c1 -> c1.getColumnName().equalsIgnoreCase(c.getColumnName()) || c1.getColumnName().substring(1, c1.getColumnName().length() - 1).equalsIgnoreCase(c.getColumnName()))
                    .findFirst().orElse(null);

            System.out.println("Expected: " + c);
            System.out.println("Actual: " + actualColumn);
            System.out.println("---");
            //assertEquals(c.getColumnName().toLowerCase(), actualColumn.getColumnName().toLowerCase());
            assertEquals(c.getDataType(), actualColumn.getDataType());
            assertEquals(c.getCharacterMaximumLength(), actualColumn.getCharacterMaximumLength());
            assertEquals(c.getNumericPrecision(), actualColumn.getNumericPrecision());
            assertEquals(c.getNumericScale(), actualColumn.getNumericScale());
            assertEquals(c.getComment(), "".equals(actualColumn.getComment()) ? null : actualColumn.getComment());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"H2"/*, "MYSQL", "POSTGRESQL", "OCEANBASE"*/})
    void schema(Database db) {
        String schema = "table_manager_test";

        TableManagerImpl tableService = TableServiceBuildUtil.createTableService(db);

        String tableName = "test_createOrUpgradeTable";

        Table table = new Table(tableName, schema);

        try {
            tableService.getSqlHelper().insert(null, "CREATE SCHEMA " + schema, null);
        } catch (Exception e) {
        }

        try {
            tableService.dropTable(table);
        } catch (Exception e) {
        }

        DataType dataType = DataType.LONGTEXT;
        if (db == Database.ORACLE) {
            dataType = DataType.CLOB;
        }
        if (db == Database.POSTGRESQL || db == Database.H2) {
            dataType = DataType.TEXT;
        }

        List<Column> columns = new ArrayList<>();
        columns.add(c("id", DataType.VARCHAR.name(), 64, null, null, true, null));
        columns.add(c("name", DataType.VARCHAR.name(), 200, null, null, true, null));
        columns.add(c("age", DataType.NUMERIC.name(), null, 6, 0, true, null));
        columns.add(c("amount", DataType.NUMERIC.name(), null, 6, 4, true, null));
        columns.add(c("update_time", DataType.TIMESTAMP.name(), null, null, null, true, null));
        columns.add(c("text2", dataType.name(), null, null, null, false, JsonTypeHandler.class));
        columns.add(c("json2", dataType.name(), null, null, null, false, JsonTypeHandler.class));

        for (Column column : columns) {
            column.setColumnName(tableService.getMetaDataHelper().getWrappedIdentifierInMeta(column.getColumnName()));
        }

        //table.getColumns().add(c("Add1", DataType.VARCHAR.name(), 20, null, null, true, null));

        table.setColumns(columns);

        tableService.dropTable(table);
        tableService.createOrUpgradeTable(table);
        assertColumnsEqual(tableService, table.getTableName(), table.getSchema(), table.getColumns(), db);
        //===============
        table.getColumns().add(c("Add1", DataType.VARCHAR.name(), 20, null, null, true, null));
        table.getColumns().add(c("add2", DataType.VARCHAR.name(), 20, null, null, true, null));
        table.getColumns().add(c("add3", DataType.VARCHAR.name(), 20, null, null, true, null));

        for (Column column : table.getColumns()) {
            column.setColumnName(tableService.getMetaDataHelper().getWrappedIdentifierInMeta(column.getColumnName()));
        }

        tableService.createOrUpgradeTable(table);
        assertColumnsEqual(tableService, table.getTableName(), table.getSchema(), table.getColumns(), db);
    }


    @ParameterizedTest
    @ValueSource(strings = {"H2"/*, "MYSQL", "POSTGRESQL", "OCEANBASE"*/})
    void comment(Database db) {
        TableManagerImpl tableService = TableServiceBuildUtil.createTableService(db);

        String tableName = "test_comment";

        Table table = new Table(tableName, null);
        table.setComment("测试表注释");

        try {
            tableService.dropTable(table);
        } catch (Exception e) {
        }

        Column c1 = c("id", DataType.VARCHAR.name(), 64, null, null, true, null);
        c1.setComment("唯一标识");

        Column c2 = c("name2", DataType.VARCHAR.name(), 64, null, null, true, null);
        c2.setComment("姓名");

        List<Column> columns = new ArrayList<>();
        columns.add(c1);
        columns.add(c2);
        for (Column column : columns) {
            column.setColumnName(tableService.getMetaDataHelper().getWrappedIdentifierInMeta(column.getColumnName()));
        }

        table.setColumns(columns);

        tableService.createOrUpgradeTable(table);
        assertColumnsEqual(tableService, table.getTableName(), table.getSchema(), table.getColumns(), db);

        Table currentTable = tableService.queryTable(table);

        assertEquals(table.getComment(), currentTable.getComment());

        //===============
        table.setComment("修改表注解");
        c2.setComment(null);
        tableService.createOrUpgradeTable(table);

        //c2设置为空，实际comment不变
        c2.setComment("姓名");
        assertColumnsEqual(tableService, table.getTableName(), table.getSchema(), table.getColumns(), db);

        Table currentTable2 = tableService.queryTable(table);
        assertEquals(table.getComment(), currentTable2.getComment());

        //===============

        c2.setComment("姓名修改");
        tableService.createOrUpgradeTable(table);
        assertColumnsEqual(tableService, table.getTableName(), table.getSchema(), table.getColumns(), db);
        Table currentTable3 = tableService.queryTable(table);
        assertEquals(table.getComment(), currentTable3.getComment());


    }


}