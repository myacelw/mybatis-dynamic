package io.github.myacelw.mybatis.dynamic.core.database.impl;

import io.github.myacelw.mybatis.dynamic.core.Database;
import io.github.myacelw.mybatis.dynamic.core.TableServiceBuildUtil;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MybatisHelperImplTest {


    @ParameterizedTest
    @ValueSource(strings = {"H2"/*, "MYSQL", "POSTGRESQL", "OCEANBASE"*/})
    void getTables(Database db) {
        TableManagerImpl tableService = TableServiceBuildUtil.createTableService(db);

        String schema = tableService.getMetaDataHelper().getWrappedIdentifierInMeta("test_mybatis_helper");
        String tableName = tableService.getMetaDataHelper().getWrappedIdentifierInMeta("test_createorupgradetable");
        String comment = "test_createOrUpgradeTable comment2";


        createTable(tableService, schema, tableName, comment);

        Table table = tableService.getMetaDataHelper().getTable(tableName, schema);
        List<Column> columns = tableService.getMetaDataHelper().getColumns(tableName, schema);

        //tableManager.dropTable(table);

        assertNotNull(table);
        assertEquals(tableName, table.getTableName());
        assertEquals(schema, table.getSchema());
        assertEquals(comment, table.getComment());

        assertEquals(7, columns.size());
        assertEquals(tableService.getMetaDataHelper().getWrappedIdentifierInMeta("id"), columns.get(0).getColumnName());
        assertEquals(tableService.getMetaDataHelper().getWrappedIdentifierInMeta("name"), columns.get(1).getColumnName());
        assertEquals(tableService.getMetaDataHelper().getWrappedIdentifierInMeta("age"), columns.get(2).getColumnName());
        assertEquals(tableService.getMetaDataHelper().getWrappedIdentifierInMeta("birthday"), columns.get(3).getColumnName());
        assertEquals("年龄", columns.get(2).getComment());
        assertEquals("生日", columns.get(3).getComment());
//        assertEquals(DataType.VARCHAR, columns.get(0).getDataTypeObj());
//        assertEquals(DataType.VARCHAR, columns.get(1).getDataTypeObj());
//        assertEquals(DataType.INTEGER, columns.get(2).getDataTypeObj());
//        assertEquals(DataType.DATETIME, columns.get(3).getDataTypeObj());
        assertEquals(8, columns.get(4).getNumericPrecision());
        assertEquals(2, columns.get(4).getNumericScale());

    }

    private void createTable(TableManagerImpl tableService, String schema, String tableName, String comment) {
        SqlSessionFactory sqlSessionFactory = tableService.getSqlHelper().getSqlSessionFactory();

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            tableService.getSqlHelper().insert(sqlSession, "CREATE SCHEMA " + schema, null);
        }

        Table table = new Table(tableName, schema);
        table.setComment(comment);

        Column id = new Column();
        id.setColumnName("id");
        id.setDataType("VARCHAR");
        id.setCharacterMaximumLength(64);

        Column name = new Column();
        name.setColumnName("name");
        name.setDataType("VARCHAR");
        name.setCharacterMaximumLength(64);

        Column age = new Column();
        age.setColumnName("age");
        age.setDataType("INTEGER");
        age.setComment("年龄");

        Column birthday = new Column();
        birthday.setColumnName("birthday");
        birthday.setDataType("DATE");
        birthday.setComment("生日");

        Column num1 = new Column();
        num1.setColumnName("num1");
        num1.setDataType("NUMERIC");
        num1.setNumericPrecision(8);
        num1.setNumericScale(2);

        Column num2 = new Column();
        num2.setColumnName("num2");
        num2.setDataType("INTEGER");

        Column num3 = new Column();
        num3.setColumnName("num3");
        num3.setDataType("DECIMAL");

        List<Column> columns = new ArrayList<>();
        columns.add(id);
        columns.add(name);
        columns.add(age);
        columns.add(birthday);
        columns.add(num1);
        columns.add(num2);
        columns.add(num3);

        table.setColumns(columns);

        try {
            tableService.dropTable(table);
        } catch (Exception e) {
        }
        tableService.createOrUpgradeTable(table);
    }

}