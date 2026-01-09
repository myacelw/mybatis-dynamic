package io.github.myacelw.mybatis.dynamic.core.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.myacelw.mybatis.dynamic.core.Database;
import io.github.myacelw.mybatis.dynamic.core.TableServiceBuildUtil;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.ModelDataLoader;
import io.github.myacelw.mybatis.dynamic.core.service.ModelService;
import io.github.myacelw.mybatis.dynamic.core.service.ModelServiceBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractCreatorFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractModifierFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.Filler;
import io.github.myacelw.mybatis.dynamic.core.util.BeanUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class DataManagerImplTest<ID> {
    private final static ObjectMapper OM = YAMLMapper.builder().addModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).build();

    static SqlSessionFactory sqlSessionFactory = TableServiceBuildUtil.createSqlSessionFactory(Database.H2);

    static ModelService modelService;

    static String currentUser = "admin";

    static ModelDataLoader modelDataLoader;

    Map<String, List<Map<String, Object>>> initDataMap;
    SqlSession sqlSession = null;

    DataManager<ID> companyDataManager;
    DataManager<ID> departmentDataManager;
    DataManager<ID> roleDataManager;
    DataManager<ID> userDataManager;
    DataManager<ID> userAddressDataManager;
    DataManager<ID> userRoleDataManager;
    DataManager<ID> userExtManager;

    @BeforeAll
    static void setUp() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger("io.github.myacelw.mybatis.dynamic");
        rootLogger.setLevel(Level.DEBUG);

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

        modelService = new ModelServiceBuilder(sqlSessionFactory).fillers(fillers).tablePrefix("d_").build();
        modelDataLoader = new ModelDataLoader(modelService);
        modelDataLoader.setIdType(String.class);
        modelDataLoader.updateAndRegister("classpath:models.json");
    }

    @BeforeEach
    void before() {
        this.initDataMap = modelDataLoader.initModelData(sqlSessionFactory, "classpath:data.json");

        List<Map<String, Object>> data = printData("select * from d_department");

        this.sqlSession = sqlSessionFactory.openSession();
        this.companyDataManager = modelService.getDataManager("Company", sqlSession);
        this.departmentDataManager = modelService.getDataManager("Department", sqlSession);
        this.roleDataManager = modelService.getDataManager("Role", sqlSession);
        this.userDataManager = modelService.getDataManager("User", sqlSession);
        this.userAddressDataManager = modelService.getDataManager("UserAddress", sqlSession);
        this.userExtManager = modelService.getDataManager("UserExt", sqlSession);
        this.userRoleDataManager = modelService.getDataManager("UserRole", sqlSession);
    }

    @SneakyThrows
    private List<Map<String, Object>> printData(String sql) {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection connection = sqlSessionFactory.openSession().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet resultSet = statement.executeQuery();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int j = 0;
                while (resultSet.next()) {
                    log.info("== Data == : {}", j++);
                    Map<String, Object> data = new HashMap<>();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        log.info("{} : {}", metaData.getColumnName(i), resultSet.getString(metaData.getColumnName(i)));
                        data.put(metaData.getColumnName(i), resultSet.getString(metaData.getColumnName(i)));
                    }
                    result.add(data);
                }
            }
        }
        return result;
    }

    @AfterEach
    void after() {
        if (sqlSession != null) {
            sqlSession.close();
        }
        for (Model model : modelService.getAllRegisteredModels()) {
            modelService.delete(model);
        }
    }

    @Test
    void query() throws JsonProcessingException {
        Condition condition = Condition.builder().eqOrIn("department.name", Arrays.asList("部门A", "其他值")).build();

        List<Map<String, Object>> result = userDataManager.queryChain()
                .where(condition)
                .desc("department.company.name")
                .page(1, 50)
                .select("*", "department.company", "userRoles.role", "userAddressList", "ext")
                .exec();


        log.info("Query Result: {}", OM.writeValueAsString(result));
        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).get("name"));
        assertNotNull(result.get(0).get(Model.FIELD_CREATE_TIME));
        assertEquals("部门A", BeanUtil.getProperty(result.get(0), "department.name"));
        assertEquals("公司A", BeanUtil.getProperty(result.get(0), "department.company.name"));
        //assertEquals("普通用户", BeanUtil.getProperty(result.get(0), "roles[0].name"));

        String city0 = (String) BeanUtil.getProperty(result.get(0), "userAddressList[0].address.city");
        String city1 = (String) BeanUtil.getProperty(result.get(0), "userAddressList[1].address.city");
        assertTrue("深圳".equals(city0) || "深圳".equals(city1));
        assertTrue("广州".equals(city0) || "广州".equals(city1));

        assertEquals("90001", BeanUtil.getProperty(result.get(0), "ext.oldSystemId"));


    }
//
//
//    @Test
//    void exists() {
//        // 获取 root logger
//        Logger rootLogger = (Logger) LoggerFactory.getLogger("ROOT");
//        // 设置 root logger 的日志级别为 DEBUG
//        rootLogger.setLevel(Level.DEBUG);
//
//        List<Join> joins = new ArrayList<>();
//        joins.add(Join.of("department", departmentDataManager));
//        joins.add(Join.of("department.company", companyDataManager));
//        joins.add(Join.of("roles", roleDataManager));
//        joins.add(Join.of("address", null));
//        joins.add(Join.of("ext", null));
//
//        Condition where = Condition.builder().eq("name", "张三").build();
//
//        boolean result = userDataManager.exists(where);
//        log.info("Query Result: {}", result);
//        assertTrue(result);
//
//        Condition condition2 = Condition.builder().eq("name", "张三2").build();
//
//        boolean result2 = userDataManager.exists(condition2);
//        log.info("Query Result2: {}", result2);
//        assertFalse(result2);
//    }
//
//    /**
//     * 按关联关系ID查询数据
//     */
//    @Test
//    void queryByRelId() throws JsonProcessingException {
//        Condition where = Condition.builder().eq("company", "c3").build();
//
//        List<Map<String, Object>> result = departmentDataManager.query(where);
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//        assertEquals(1, result.size());
//        assertEquals("d3", BeanUtil.getProperty(result.get(0), "id"));
//        assertEquals("部门C", BeanUtil.getProperty(result.get(0), "name"));
//        assertEquals("c3", BeanUtil.getProperty(result.get(0), "company"));
//    }
//
//    @Test
//    void selectField() throws JsonProcessingException {
//        List<Join> joins = new ArrayList<>();
//        joins.add(Join.of("department", departmentDataManager));
//        joins.add(Join.of("department.company", companyDataManager, Collections.emptyList()));
//        joins.add(Join.of("roles", roleDataManager, Arrays.asList("name")));
//        joins.add(Join.of("address", addressDataManager, Arrays.asList("city")));
//
//        Condition where = Condition.builder().eq("department.name", "部门A").build();
//
//        List<Map<String, Object>> result = userDataManager.queryChain().where(where).desc("department.company.name").page(1, 50).joins(joins).selectField("name").exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//        assertEquals(1, result.size());
//        assertEquals("张三", result.get(0).get("name"));
//        assertNull(result.get(0).get(Model.FIELD_CREATE_TIME));
//
//        assertEquals("部门A", BeanUtil.getProperty(result.get(0), "department.name"));
//        assertNotNull(BeanUtil.getProperty(result.get(0), "department.company.id"));
//        assertNull(BeanUtil.getProperty(result.get(0), "department.company.name"));
//
//        Map user0 = result.get(0);
//        List<?> roles = (List<?>) user0.get("roles");
//        assertEquals(2, roles.size());
//        assertEquals(new HashSet<>(Arrays.asList("普通用户", "管理员")), new HashSet<>(Arrays.asList(BeanUtil.getProperty(roles, "[0].name"), BeanUtil.getProperty(roles, "[1].name"))));
//        assertNotNull(BeanUtil.getProperty(result.get(0), "address[0].city"));
//        assertNull(BeanUtil.getProperty(result.get(0), "address[0].postcode"));
//    }
//
//
//    @Test
//    void permission() throws JsonProcessingException {
//        Permission permission = new Permission();
//        permission.setFieldRights(new ArrayList<>());
//        //只有name字段权限
//        permission.getFieldRights().add("name");
//
//        //只能查询"部门A"的数据
//        permission.setDataRights(SimpleCondition.eq("department", initDataMap.get("Department").get(0).get("id")));
//
//        DataManager userDataManager2 = modelService.createDataManager(userModel, permission);
//
//        List<Map<String, Object>> result = userDataManager2.query(null);
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//        assertEquals(1, result.size());
//        assertEquals("张三", result.get(0).get("name"));
//        assertNull(BeanUtil.getProperty(result.get(0), "department"));
//        assertNull(BeanUtil.getProperty(result.get(0), Model.FIELD_CREATOR));
//        assertNull(BeanUtil.getProperty(result.get(0), Model.FIELD_CREATE_TIME));
//        assertNull(BeanUtil.getProperty(result.get(0), Model.FIELD_UPDATE_TIME));
//
//        try {
//            List<Map<String, Object>> result3 = userDataManager2.query(SimpleCondition.eq("department", "111"));
//            fail();
//        } catch (BaseException e) {
//            //没有字段权限，但使用该字段查询应抛出异常
//            log.info("Expected exception: {}", e.getMessage());
//        }
//
//    }
//
//
//    @Test
//    void fill() throws JsonProcessingException {
//        List<Map<String, Object>> result = userDataManager.query(SimpleCondition.startsWith("name", "张"));
//
//        userDataManager.fillChain().data(result)
//                .fillField("department", departmentDataManager)
//                .fillField("roles", roleDataManager)
//                .fillField("address", addressDataManager)
//                .fillField("ext", userExtManager)
//                .exec();
//
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//        assertEquals(1, result.size());
//        assertEquals("张三", result.get(0).get("name"));
//        assertEquals("部门A", BeanUtil.getProperty(result.get(0), "department.name"));
//        assertEquals("普通用户", BeanUtil.getProperty(result.get(0), "roles[0].name"));
//        assertEquals("广州", BeanUtil.getProperty(result.get(0), "address[0].city"));
//        assertEquals("90001", BeanUtil.getProperty(result.get(0), "ext.oldSystemId"));
//    }
//
//    /**
//     * 反向关联查询测试
//     */
//    @Test
//    void queryBackLink() throws JsonProcessingException {
//        Model userModel = models.get("User");
//        DataManager<String> userDataManager = modelService.createDataManager(userModel, null);
//
//        Model departmentModel = models.get("Department");
//        DataManager<String> departmentDataManager = modelService.createDataManager(departmentModel, null);
//
//        Model companyModel = models.get("Company");
//        DataManager<String> companyDataManager = modelService.createDataManager(companyModel, null);
//
//        Model roleModel = models.get("Role");
//        DataManager<String> roleDataManager = modelService.createDataManager(roleModel, null);
//
//        List<Join> joins = new ArrayList<>();
//        joins.add(Join.of("users", userDataManager));
//        joins.add(Join.of("company", companyDataManager));
//        joins.add(Join.of("users.roles", roleDataManager));
//        joins.add(Join.of("users.address", addressDataManager));
//
//        List<Map<String, Object>> result = departmentDataManager.queryChain().where(SimpleCondition.startsWith("users.name", "张三")).page(1, 50).joins(joins).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//
//        Map<String, Object> department0 = result.get(0);
//        List<?> users = (List<?>) department0.get("users");
//        Map user0 = (Map) users.get(0);
//        List<?> roles = (List<?>) user0.get("roles");
//
//        assertEquals(1, result.size());
//        assertEquals("部门A", BeanUtil.getProperty(result.get(0), "name"));
//        assertEquals("公司A", BeanUtil.getProperty(result.get(0), "company.name"));
//        assertEquals("张三", BeanUtil.getProperty(user0, "name"));
//        assertEquals(2, roles.size());
//
//        assertEquals(new HashSet<>(Arrays.asList("普通用户", "管理员")), new HashSet<>(Arrays.asList(BeanUtil.getProperty(roles, "[0].name"), BeanUtil.getProperty(roles, "[1].name"))));
//
//        String city0 = (String) BeanUtil.getProperty(user0, "address[0].city");
//        String city1 = (String) BeanUtil.getProperty(user0, "address[1].city");
//
//        assertTrue("深圳".equals(city0) || "深圳".equals(city1));
//        assertTrue("广州".equals(city0) || "广州".equals(city1));
//    }
//
//    /**
//     * 反向关联查询测试
//     */
//    @Test
//    void refRelExists() throws JsonProcessingException {
//        Model userModel = models.get("User");
//        DataManager<String> userDataManager = modelService.createDataManager(userModel, null);
//
//        Model departmentModel = models.get("Department");
//        DataManager<String> departmentDataManager = modelService.createDataManager(departmentModel, null);
//
//        Model companyModel = models.get("Company");
//        DataManager<String> companyDataManager = modelService.createDataManager(companyModel, null);
//
//        Model roleModel = models.get("Role");
//        DataManager<String> roleDataManager = modelService.createDataManager(roleModel, null);
//
//        List<Join> joins = new ArrayList<>();
//        joins.add(Join.existsOf(userDataManager));
//        joins.add(Join.of("company", companyDataManager));
//
//        List<Map<String, Object>> result = departmentDataManager.queryChain().where(ExistsCondition.of("users", SimpleCondition.startsWith("name", "张三"))).page(1, 50).joins(joins).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//        assertEquals(1, result.size());
//        assertEquals("部门A", BeanUtil.getProperty(result.get(0), "name"));
//        assertEquals("公司A", BeanUtil.getProperty(result.get(0), "company.name"));
//    }
//
//    @Test
//    void queryPlain() throws JsonProcessingException {
//        List<Join> joins = new ArrayList<>();
//        joins.add(Join.of("address", addressDataManager));
//
//        List<Map<String, Object>> result = userDataManager.queryChain().plain().where(SimpleCondition.startsWith("name", "张三")).asc("address.city").joins(joins).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//        assertEquals(2, result.size());
//        assertEquals("张三", result.get(0).get("name"));
//        assertEquals("广州", BeanUtil.getProperty(result.get(0), "address.city"));
//        assertEquals("深圳", BeanUtil.getProperty(result.get(1), "address.city"));
//    }
//
//    @Test
//    void queryCallBack() throws JsonProcessingException {
//        List<Join> joins = new ArrayList<>();
//        joins.add(Join.of("address", addressDataManager));
//
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        userDataManager.queryCallBackChain().where(SimpleCondition.startsWith("name", "张三")).asc("address.city").joins(joins).handler(t -> {
//            result.add(t.getResultObject());
//        }).exec();
//
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//        assertEquals(2, result.size());
//        assertEquals("张三", result.get(0).get("name"));
//        assertEquals("广州", BeanUtil.getProperty(result.get(0), "address.city"));
//        assertEquals("深圳", BeanUtil.getProperty(result.get(1), "address.city"));
//    }
//
//    @Test
//    void aggQuery() throws JsonProcessingException {
//        List<Join> joins = new ArrayList<>();
////        joins.add(Join.of("department", departmentDataManager));
////        joins.add(Join.of("department.company", companyDataManager));
//        joins.add(Join.of("address", addressDataManager));
//
//
//        List<Map<String, Object>> result1 = userDataManager.queryChain().plain().joins(joins).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result1));
//
//        List<AggSelectItem> aggSelectItems = new ArrayList<>();
//        aggSelectItems.add(AggSelectItem.of("address.postcode"));
//        aggSelectItems.add(AggSelectItem.of("*", AggFunction.COUNT));
//
//        List<Map<String, Object>> result = userDataManager.aggQueryChain().aggSelectItems(aggSelectItems).joins(joins).page(1, 50).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//
//        assertEquals(2, result.size());
//        assertEquals("100000", BeanUtil.getProperty(result.get(0), "address.postcode"));
//        assertEquals(3, (Integer) BeanUtil.getProperty(result.get(0), "*_count"));
//    }
//
//    @Test
//    void aggQueryForEmtry() throws JsonProcessingException {
//        List<Join> joins = new ArrayList<>();
////        joins.add(Join.of("department", departmentDataManager));
////        joins.add(Join.of("department.company", companyDataManager));
//        joins.add(Join.of("address", addressDataManager));
//
//
//        List<Map<String, Object>> result1 = userDataManager.queryChain().plain().joins(joins).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result1));
//
//        List<AggSelectItem> aggSelectItems = new ArrayList<>();
//        aggSelectItems.add(AggSelectItem.of("address.postcode"));
//        aggSelectItems.add(AggSelectItem.of("*", AggFunction.COUNT));
//
//        List<Map<String, Object>> result = userDataManager.aggQueryChain().aggSelectItems(aggSelectItems).joins(joins).max("id", "id").where(b -> b.eq("address.postcode", "1002000")).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//
//        assertEquals(0, result.size());
//
//    }
//
//
//    @Test
//    void select() throws JsonProcessingException {
//        List<Join> joins = new ArrayList<>();
//        joins.add(Join.of("address", addressDataManager));
//
//        CustomSelectField customSelectField = new CustomSelectField();
//        customSelectField.setName("fun1");
//        customSelectField.setFields(Collections.singletonList("address.street"));
//        customSelectField.setSqlTemplate("SUBSTRING($COL FROM 1 FOR #{EXPR})");
//        customSelectField.setValue(3);
//
//        List<Map<String, Object>> result = userDataManager.queryChain().select(customSelectField).joins(joins).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//    }
//
//    @Test
//    void aggQuery2() throws JsonProcessingException {
//        List<Join> joins = new ArrayList<>();
//
//        List<AggSelectItem> aggSelectItems = new ArrayList<>();
//        aggSelectItems.add(AggSelectItem.of("name", AggFunction.LISTAGG));
//
//        List<Map<String, Object>> result = userDataManager.aggQueryChain().aggSelectItems(aggSelectItems).page(1, 50).joins(joins).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//    }
//
//
//    @Test
//    void existsCondition() throws JsonProcessingException {
//        List<Join> joins = new ArrayList<>();
//        joins.add(Join.existsOf(addressDataManager));
//        List<Map<String, Object>> result = userDataManager.queryChain().plain().where(ExistsCondition.of("address", SimpleCondition.eq("city", "广州"))).joins(joins).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//        assertEquals(1, result.size());
//        assertEquals("张三", result.get(0).get("name"));
//    }
//
//    @Test
//    void existsCondition2() throws JsonProcessingException {
//        List<Join> joins = new ArrayList<>();
//        joins.add(Join.existsOf(roleDataManager));
//        List<Map<String, Object>> result = userDataManager.queryChain().plain().where(ExistsCondition.of("roles", SimpleCondition.in("name", Arrays.asList("普通用户", "AAA")))).joins(joins).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//        assertEquals(1, result.size());
//        assertEquals("张三", result.get(0).get("name"));
//    }
//
//    /**
//     * 自关联查询测试， modifier修改人字段 改为关联字段，关联到User表本身
//     */
//    @Test
//    void selfLink() throws JsonProcessingException {
//        Map<String, Object> u1 = new HashMap<>(this.initDataMap.get("User").get(0));
//        Map<String, Object> u2 = new HashMap<>(this.initDataMap.get("User").get(0));
//        String modifier = (String) u2.get("id");
//        this.currentUser = modifier;
//        userDataManager.updateChain().data(u1).force().exec();
//
//        Model model = new Model();
//        model.setName(this.userModel.getName());
//        this.userModel.getFields().forEach(field -> {
//            if (!Model.FIELD_MODIFIER.equals(field.getName())) {
//                model.getFields().add(field);
//            }
//        });
//
//        ToOneField m = Field.manyToOne(Model.FIELD_MODIFIER+"Obj", this.userModel.getName(), Model.FIELD_MODIFIER).build();
//        model.getFields().add(m);
//
//        DataManager<String> dataManager = this.modelService.createDataManager(model, null);
//
//        Map<String, Object> u3 = dataManager.getByIdChain().id((String) u1.get("id")).join(Join.of(Model.FIELD_MODIFIER, dataManager)).exec();
//        log.info("Query Result: {}", OM.writeValueAsString(u3));
//        Map<String, Object> u4 = (Map<String, Object>) u3.get(Model.FIELD_MODIFIER);
//        assertEquals(modifier, u4.get("id"));
//    }
//
//    /**
//     * 递归查询测试
//     */
//    @Test
//    void recursive() throws JsonProcessingException {
//        List<Map<String, Object>> result = this.departmentDataManager.queryRecursiveList(SimpleCondition.eq("name", "部门A"), true);
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//        assertEquals(4, result.size());
//
//        List<Map<String, Object>> result2 = this.departmentDataManager.queryRecursiveListChain()
//                .initNodeCondition(SimpleCondition.eq("name", "部门A-1-1"))
//                .where(SimpleCondition.ne("name", "部门A-1-1"))
//                .recursiveDown(false)
//                .join(Join.of("company", companyDataManager))
//                .exec();
//        log.info("Query Result2: {}", OM.writeValueAsString(result2));
//        assertEquals(2, result2.size());
//
//        Map<String, Object> result3 = this.departmentDataManager.getRecursiveTreeById((String) initDataMap.get("Department").get(0).get("id"));
//        log.info("Query Result3: {}", OM.writeValueAsString(result3));
//        assertEquals(2, ((List) result3.get("children")).size());
//    }
//
//    @Test
//    void countRecursive() throws JsonProcessingException {
//        long result = this.departmentDataManager.countRecursive(SimpleCondition.eq("name", "部门A"), null, true);
//        log.info("Query Result: {}", OM.writeValueAsString(result));
//        assertEquals(4L, result);
//    }
//
//
//    @Test
//    void required() throws JsonProcessingException {
//        Map<String, Object> c1 = new HashMap<>();
//        c1.put("name", "Z");
//        String c1Id = companyDataManager.insert(c1);
//
//        try {
//            c1.put("name", null);
//            companyDataManager.update(c1Id, c1);
//            fail();
//        } catch (BaseException e) {
//            log.warn("========= {}", e.getMessage());
//            assertTrue(e.getMessage().contains("不能为空"));
//        }
//
//        try {
//            Map<String, Object> c2 = new HashMap<>();
//            companyDataManager.insert(c2);
//            fail();
//        } catch (BaseException e) {
//            log.warn("========= {}", e.getMessage());
//            assertTrue(e.getMessage().contains("不能为空"));
//        }
//
//        try {
//            Map<String, Object> u1 = new HashMap<>();
//            u1.put("name", "U1");
//            u1.put("address", Collections.emptyList());
//            userDataManager.insert(u1);
//            fail();
//        } catch (BaseException e) {
//            log.warn("========= {}", e.getMessage());
//            assertTrue(e.getMessage().contains("不能为空"));
//        }
//
//        try {
//            Map<String, Object> u1 = new HashMap<>();
//            u1.put("name", "U1");
//            Map<String, Object> u1Addr1 = new HashMap<>();
//            u1Addr1.put("city", "");
//            u1.put("address", Collections.singletonList(u1Addr1));
//            userDataManager.insert(u1);
//            fail();
//        } catch (BaseException e) {
//            log.warn("========= {}", e.getMessage());
//            assertTrue(e.getMessage().contains("不能为空"));
//        }
//
//        try {
//            Map<String, Object> u1 = new HashMap<>(this.initDataMap.get("User").get(0));
//            u1.put("address", null);
//            userDataManager.updateChain().data(u1).force().exec();
//        } catch (BaseException e) {
//            fail(e);
//        }
//
//        try {
//            Map<String, Object> u1 = new HashMap<>(this.initDataMap.get("User").get(0));
//            u1.put("address", new ArrayList<>());
//            userDataManager.update((String) u1.get("id"), u1);
//            fail();
//        } catch (BaseException e) {
//            log.warn("========= {}", e.getMessage());
//            assertTrue(e.getMessage().contains("不能为空"));
//        }
//
//        try {
//            Map<String, Object> u1 = new HashMap<>(this.initDataMap.get("User").get(0));
//            Map<String, Object> u1Addr1 = new HashMap<>();
//            u1Addr1.put("city", null);
//            u1.put("address", Collections.singletonList(u1Addr1));
//            userDataManager.update((String) u1.get("id"), u1);
//            fail();
//        } catch (BaseException e) {
//            log.warn("========= {}", e.getMessage());
//            assertTrue(e.getMessage().contains("不能为空"));
//        }
//
//        try {
//            Map<String, Object> u1 = new HashMap<>(this.initDataMap.get("User").get(0));
//            Map<String, Object> u1Addr1 = ((List<Map<String, Object>>) u1.get("address")).get(0);
//            u1Addr1.remove("city");
//            userDataManager.update((String) u1.get("id"), u1);
//        } catch (BaseException e) {
//            log.error("========= {}", e.getMessage());
//            fail();
//        }
//
//    }
//
//    @Test
//    void insert() throws JsonProcessingException {
//        // 获取 root logger
//        Logger rootLogger = (Logger) LoggerFactory.getLogger("ROOT");
//        // 设置 root logger 的日志级别为 DEBUG
//        rootLogger.setLevel(Level.DEBUG);
//
//        Model model = new Model();
//        model.setName("insert_test");
//
//        model.getFields().add(Field.integer(Model.FIELD_ID));
//        model.getFields().add(Field.string("name", 20));
//        model.setKeyGeneratorMode(KeyGeneratorMode.AUTO);
//
//        modelService.update(model);
//        DataManager<String> dataManager = this.modelService.createDataManager(model, null);
//
//        Map<String, Object> u1 = new HashMap<>();
//        u1.put("name", "王五");
//
//        Object id = dataManager.insert(u1);
//
//        log.info("Query Result: {}", OM.writeValueAsString(u1));
//        assertEquals("王五", u1.get("name"));
//        assertTrue(u1.containsKey("id"));
//    }
//
//
//    @Test
//    void defaultValue() throws JsonProcessingException {
//        Model model = this.userModel.clone();
//        model.setTableName("user_defaultValue_test");
//        model.findBasicField("name").setDefaultValue("王五");
//
//        Map<String, Object> u1 = new HashMap<>();
//        u1.put("address", Collections.singletonList(new HashMap<>()));
//        u1.put("ext", new HashMap<>());
//
//        modelService.update(model);
//        DataManager<String> dataManager = this.modelService.createDataManager(model, null);
//
//        String id = dataManager.insert(u1);
//
//        Map<String, Object> u2 = dataManager.getById(id);
//        log.info("Query Result: {}", OM.writeValueAsString(u2));
//        assertEquals("王五", u2.get("name"));
//    }
//
//
//    @Test
//    void update() throws JsonProcessingException {
//        Model model = this.userModel.clone();
//        model.setTableName("user_update_test");
//
//        Map<String, Object> u1 = new HashMap<>();
//        u1.put("name", "王五");
//        Map<String, String> ext = new HashMap<>();
//        ext.put("oldSystemId", "555555");
//        u1.put("ext", ext);
//
//        modelService.update(model);
//        DataManager<String> dataManager = this.modelService.createDataManager(model, null);
//
//        String id = dataManager.insert(u1);
//
//        Map<String, Object> u2 = new HashMap<>();
//        u2.put("id", id);
//        u2.put("name", "王小五");
//        u2.put("ext", null); //为空时不更新
//        dataManager.update(u2);
//
//        Map<String, Object> u3 = new HashMap<>();
//        u3.put("id", id);
//        u3.put("name", "王小五");
//        u3.put("ext", Collections.emptyMap()); //为空时不更新
//        dataManager.update(u3);
//
//        Map<String, Object> result1 = dataManager.getById(id);
//
//
//        Map<String, Object> u4 = new HashMap<>();
//        u4.put("id", id);
//        u4.put("name", "王小五");
//        Map<String, String> value2 = new HashMap<>();
//        value2.put("oldSystemId", "6666666");
//        u4.put("ext", value2);
//        dataManager.update(u4);
//    }

}