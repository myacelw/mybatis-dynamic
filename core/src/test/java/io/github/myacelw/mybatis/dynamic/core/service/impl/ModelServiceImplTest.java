package io.github.myacelw.mybatis.dynamic.core.service.impl;

import io.github.myacelw.mybatis.dynamic.core.Database;
import io.github.myacelw.mybatis.dynamic.core.TableServiceBuildUtil;
import io.github.myacelw.mybatis.dynamic.core.database.impl.TableManagerImpl;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.AlterOrDropStrategy;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.ModelService;
import io.github.myacelw.mybatis.dynamic.core.service.ModelServiceBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractCreatorFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractModifierFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.Filler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class ModelServiceImplTest {

    ModelServiceImpl service;

    static TableManagerImpl tableService = TableServiceBuildUtil.createTableService(Database.H2);
    static SqlSessionFactory sqlSessionFactory = tableService.getSqlHelper().getSqlSessionFactory();

    static String schema = "test_model_service";

    static {
        try {
            tableService.getSqlHelper().insert(null, "CREATE SCHEMA " + schema, null);
        } catch (Exception e) {

        }
    }

    Model m;

    Model userTableModel;

    Model itemTableModel;

    Model groupModel;

    String currentUser = "admin";


    public ModelServiceImplTest() {

        List<Filler> fillers = new ArrayList<>();
        fillers.add(new AbstractCreatorFiller() {
            @Override
            protected String getCurrentUser() {
                return currentUser;
            }
        });

        fillers.add(new AbstractModifierFiller() {
            @Override
            protected String getCurrentUser() {
                return currentUser;
            }
        });

        this.service = new ModelServiceBuilder(sqlSessionFactory)
                .rowLimit(3000)
                .timeoutSeconds(30)
                .tablePrefix("d_")
                .fillers(fillers)
                .build();

        m = new Model();
        m.addCommonFieldsIfNotExist(String.class);
        m.setName("test");
        m.setSchema(schema);
        m.getFields().add(Field.string("name", 100));
        m.getFields().add(Field.string("text", null));
        m.getFields().add(Field.of("userAge", int.class));
        m.getFields().add(Field.of("valid", Boolean.class));
        m.getFields().add(Field.of("amount", Float.class));
        m.getFields().add(Field.manyToOne("groupId", "group"));
        m.getFields().add(Field.fieldGroup("fieldGroup", Field.string("name", 20), Field.string("name2", 20)));

        m.getFields().add(Field.oneToMany("itemList", "itemTable", "testId"));
        //m.getFields().add(Field.manyToMany("userIds", "userTable"));

        service.delete(m);
        service.update(m);

        //===

        userTableModel = new Model();
        userTableModel.addCommonFieldsIfNotExist(String.class);
        userTableModel.setName("userTable");
        userTableModel.setSchema(schema);
        userTableModel.getFields().add(Field.string("userName", 100));
        service.update(userTableModel);

        //===

        itemTableModel = new Model();
        itemTableModel.addCommonFieldsIfNotExist(String.class);
        itemTableModel.setName("itemTable");
        itemTableModel.setSchema(schema);
        itemTableModel.getFields().add(Field.string("name", 100));
        itemTableModel.getFields().add(Field.string("testId", 100));
        service.update(itemTableModel);

        //===

        groupModel = new Model();
        groupModel.addCommonFieldsIfNotExist(String.class);
        groupModel.setName("group");
        groupModel.setSchema(schema);
        groupModel.getFields().add(Field.string("groupName", 100));
        service.update(groupModel);
    }

    @AfterEach
    public void a() {
        try {
            tableService.getSqlHelper().update(null, "DROP SCHEMA " + schema + " CASCADE", null);
            tableService.getSqlHelper().update(null, "CREATE SCHEMA " + schema, null);
        } catch (Exception e) {
        }
    }

    @Test
    public void simple() {
        ModelServiceImpl modelService = service;

        Model model = new Model();
        model.addCommonFieldsIfNotExist(String.class);
        model.setName("users");
        model.setSchema(schema);
        model.getFields().add(Field.string("name", 100));
        model.getFields().add(Field.of("age", short.class));
        model.getFields().add(Field.bool("status"));

        modelService.delete(model);

        // 创建或更新模型对应数据库表
        modelService.update(model);

        // 得到模型的数据管理器
        DataManager<?> dataManager = modelService.createDataManager(model, null, null);

        // 插入数据
        Map<String, Object> data = new HashMap<>();
        data.put("name", "lisi");
        data.put("age", (short) 20);
        data.put("status", true);

        Object id = dataManager.insert(data);

        // 查询数据
        List<Map<String, Object>> result = dataManager.query(b -> b.eq("name", "lisi"));

        log.info("========== result:{}", result);

        assertEquals(1, result.size());
        assertEquals((short) 20, result.get(0).get("age"));
        assertEquals(true, result.get(0).get("status"));
        assertEquals(LocalDateTime.class, result.get(0).get("createTime").getClass());

        modelService.delete(model);
    }

    @Test
    public void keyword() {
        ModelServiceImpl modelService = service;

        // 创建模型
        Model model = new Model();
        model.addCommonFieldsIfNotExist(String.class);
        model.setName("中文表名");
        model.setSchema(schema);
        model.getFields().add(Field.of("select", Integer.class));
        model.getFields().add(Field.of("中文字段名", Boolean.class));

        modelService.delete(model);
        // 创建或更新模型对应数据库表
        modelService.update(model);

        // 得到模型的数据管理器
        DataManager<?> dataManager = modelService.createDataManager(model, null, null);

        // 插入数据
        Map<String, Object> data = new HashMap<>();
        data.put("select", 100);
        data.put("中文字段名", true);
        Object id = dataManager.insert(data);

        // 查询数据
        List<Map<String, Object>> result = dataManager.query(b -> b.eq("select", 100));

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).get("select"));
        assertEquals(true, result.get(0).get("中文字段名"));
        assertEquals(LocalDateTime.class, result.get(0).get("createTime").getClass());

        // ===
        Model model2 = new Model();
        model2.setName("中文表名");
        model2.setSchema(schema);
        model2.getFields().add(Field.string("select", 100));
        model2.getFields().add(Field.string("中文字段名", 100));

        modelService.update(model2);

    }

    @Test
    public void index() {
        ModelService modelService = service;

        // 创建模型
        Model model = new Model();
        model.addCommonFieldsIfNotExist(String.class);
        model.setName("index测试");
        model.setSchema(schema);

        BasicField f1 = Field.string("name", 100);
        f1.getColumnDefinition().setIndex(true);

        BasicField f2 = Field.string("中文字段名", 100);
        f2.getColumnDefinition().setIndex(true);

        BasicField f3 = Field.string("addIndex", 100);

        model.getFields().add(f1);
        model.getFields().add(f3);

        // 创建或更新模型对应数据库表
        modelService.update(model);

        List<Column> columns = tableService.getCurrentTableColumns(new Table(tableService.getMetaDataHelper().getWrappedIdentifierInMeta("d_index测试"), schema));
        Optional<String> index = columns.stream().filter(t -> t.getColumnName().equalsIgnoreCase("name")).map(t -> t.getIndexName()).findFirst();
        assertTrue(index.isPresent());
        log.info("index name: {}", index);

        // ====
        f1.getColumnDefinition().setIndex(false);
        f3.getColumnDefinition().setIndex(true);
        modelService.update(model);

        List<Column> columns3 = tableService.getCurrentTableColumns(new Table(tableService.getMetaDataHelper().getWrappedIdentifierInMeta("d_index测试"), schema));
        Optional<String> index3 = columns3.stream().filter(t -> t.getColumnName().equalsIgnoreCase("add_Index")).map(t -> t.getIndexName()).findFirst();
        assertTrue(index3.isPresent());
        log.info("index3 name: {}", index3);

        Optional<String> index4 = columns3.stream().filter(t -> t.getColumnName().equalsIgnoreCase("name")).map(t -> t.getIndexName()).filter(t -> t != null).findFirst();
        assertFalse(index4.isPresent());

    }


    @Test
    public void columnAlertStrategy() {
        ModelService modelService = service;

        // 创建模型
        Model model = new Model();
        model.addCommonFieldsIfNotExist(String.class);
        model.setName("columnAlertStrategy测试");
        model.setSchema(schema);

        BasicField f1 = Field.stringBuilder("name").characterMaximumLength(100).ddlIndex(true).build();
        BasicField f2 = Field.string("FORCE_ALTER", 100);
        BasicField f3 = Field.string("IGNORE_ALTER", 100);
        BasicField f4 = Field.string("DROP_AND_RECREATE", 100);
        BasicField f5 = Field.string("DROP", 100);

        model.getFields().add(f1);
        model.getFields().add(f2);
        model.getFields().add(f3);
        model.getFields().add(f4);
        model.getFields().add(f5);


        modelService.update(model);

        // ========

        f1.getColumnDefinition().setCharacterMaximumLength(20);
        f1.getColumnDefinition().setAlterOrDropStrategy(AlterOrDropStrategy.ALTER);

        f2.setJavaClass(Integer.class);
        f2.getColumnDefinition().setAlterOrDropStrategy(AlterOrDropStrategy.ALTER);

        f3.setJavaClass(Integer.class);
        f3.getColumnDefinition().setAlterOrDropStrategy(AlterOrDropStrategy.IGNORE);

        f4.setJavaClass(Integer.class);
        f4.getColumnDefinition().setAlterOrDropStrategy(AlterOrDropStrategy.DROP_AND_RECREATE);

//        sf2.setJavaClass(Integer.class);
//        sf2.putExtProperty(Field.EXT_PROPERTY_COLUMN_ALTER_STRATEGY, ColumnAlterStrategy.FORCE_ALTER);

        model.getFields().removeIf(t -> t.getName().equals("DROP"));
        model.getTableDefine().setDropColumnNames(Collections.singletonList("DROP"));

        modelService.update(model);

        // ========

        List<Column> columns = tableService.getCurrentTableColumns(tableService.getMetaDataHelper().getWrappedIdentifierInMeta("d_column_alert_strategy测试"), schema);
        Optional<Integer> maximumLength = columns.stream().filter(t -> t.getColumnName().equalsIgnoreCase("name")).map(t -> t.getCharacterMaximumLength()).findFirst();
        assertEquals(20, maximumLength.get());

        Optional<String> dataType = columns.stream().filter(t -> t.getColumnName().equalsIgnoreCase("FORCE_ALTER")).map(t -> t.getDataType()).findFirst();
        assertTrue("INTEGER".equals(dataType.get()) || "INT".equals(dataType.get()) || "NUMERIC".equals(dataType.get()));

        Optional<String> dataType3 = columns.stream().filter(t -> t.getColumnName().equalsIgnoreCase("IGNORE_ALTER")).map(t -> t.getDataType()).findFirst();
        assertEquals("VARCHAR", dataType3.get());

        Optional<String> dataType4 = columns.stream().filter(t -> t.getColumnName().equalsIgnoreCase("DROP_AND_RECREATE")).map(t -> t.getDataType()).findFirst();
        assertTrue("INTEGER".equals(dataType4.get()) || "INT".equals(dataType4.get()) || "NUMERIC".equals(dataType4.get()));

        Optional<Column> column5 = columns.stream().filter(t -> t.getColumnName().equalsIgnoreCase("DROP")).findFirst();
        assertFalse(column5.isPresent());
    }

    @Test
    public void rename() {
        ModelServiceImpl modelService = service;

        // 创建模型
        Model model = new Model();
        model.addCommonFieldsIfNotExist(String.class);
        model.setName("老表名");
        model.setSchema(schema);

        Field oldField = Field.string("oldFieldName", 100);

        model.getFields().add(oldField);

        // 创建或更新模型对应数据库表
        modelService.update(model);

        model.getTableDefine().setOldTableNames(Collections.singletonList("d_老表名"));
        model.setName("新表名");
        model.setTableName(null);

        BasicField newField = Field.string("newFieldName", 100);
        newField.getColumnDefinition().setOldColumnNames(Collections.singletonList("old_field_name"));
        model.getFields().add(newField);
        model.getFields().remove(oldField);

        modelService.update(model);
        List<Column> columns = tableService.getCurrentTableColumns("d_新表名", schema);
        assertTrue(columns.size() > 0);

        System.out.println(columns.get(0));

        modelService.delete(model);
    }


    private static void lowerCase(List<Map> data) {
        data.forEach(t -> {
            for (Object k : new ArrayList(t.keySet())) {
                Object value = t.remove(k);
                t.put(((String) k).toLowerCase(), value);
            }
        });
    }

    @Test
    public void multiIdFieldTest() {
        Model model = new Model();
        model.setName("multiIdFieldTest");
        model.setSchema(schema);

        model.addField(Field.stringBuilder("id1").characterMaximumLength(32).ddlComment("主键1").build());
        model.addField(Field.stringBuilder("id2").characterMaximumLength(32).ddlComment("主键2").build());
        model.addField(Field.stringBuilder("name").characterMaximumLength(20).build());

        model.setPrimaryKeyFields(new String[]{"id1", "id2"});

        service.delete(model);
        service.update(model);

        DataManager<Object[]> manager = service.createDataManager(model, null, null);
        Map<String, Object> data1 = new HashMap<>();
        data1.put("id1", "a1");
        data1.put("id2", "b1");
        data1.put("name", "name1");

        Object[] id = manager.insert(data1);
        log.info("id1:{}, id2:{}", id[0], id[1]);

        Map<String, Object> data2 = new HashMap<>();
        data2.put("id1", "a2");
        data2.put("id2", "b2");
        data2.put("name", "name2");

        Object[] id2 = manager.insert(data2);
        log.info("id1:{}, id2:{}", id2[0], id2[1]);

        manager.update(id, Collections.singletonMap("name", "name111"));

        Map<String, Object> result = manager.getById(id);
        log.info("result:{}", result);
        assertEquals("a1", result.get("id1"));
        assertEquals("b1", result.get("id2"));
        assertEquals("name111", result.get("name"));
    }


    @Test
    public void bytesField() {
        Model m2 = new Model();
        m2.setName("bytesField");
        m2.setSchema(schema);

        m2.getFields().add(Field.of("bytes", byte[].class));
        m2.addStringIdFieldIfNotExist();
        service.delete(m2);
        service.update(m2);

        DataManager<String> manager = service.createDataManager(m2, null, null);
        Map<String, Object> data1 = new HashMap<>();
        data1.put("bytes", "123".getBytes());

        String id = manager.insert(data1);

        Map<String, Object> result = manager.getById(id);
        log.info("result:{}", result);
        assertEquals("123", new String((byte[]) result.get("bytes")));
    }


    @Test
    public void secretField() {
        Model m2 = new Model();
        m2.addStringIdFieldIfNotExist();
        m2.setName("secretFieldTest");
        m2.setSchema(schema);

        m2.getFields().add(Field.stringBuilder("secretField").characterMaximumLength(100).typeHandlerClass(MySecretTypeHandler.class).build());
        //service.delete(m2);
        service.update(m2);

        DataManager<String> manager = (DataManager) service.createDataManager(m2, null, null);
        Map<String, Object> data1 = new HashMap<>();
        data1.put("secretField", "abc123");

        String id = manager.insert(data1);
        Map<String, Object> result = manager.getById(id);
        log.info("result:{}", result);
        assertEquals("abc123", result.get("secretField"));

        List<Map> actualItemData = this.tableService.getSqlHelper().queryList(null, "select * from " + (schema == null ? "" : schema + ".") + "d_secret_field_test", null, Map.class);
        System.out.println(actualItemData.get(0));
        assertEquals("SECRET: YWJjMTIz", actualItemData.get(0).get("secret_field"));
    }

}