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
import io.github.myacelw.mybatis.dynamic.core.metadata.field.*;
import io.github.myacelw.mybatis.dynamic.core.metadata.partition.KeyPartition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.ModelServiceBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.entity.*;
import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractCreatorFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractModifierFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.Filler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Class2ModelTransferImplTest {
    private final static ObjectMapper OM = YAMLMapper.builder().addModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).build();

    static SqlSessionFactory sqlSessionFactory = TableServiceBuildUtil.createSqlSessionFactory(Database.H2);

    String currentUser = "admin";

    @Test
    void getEntityModel() throws JsonProcessingException {
        Model model = new Class2ModelTransferImpl().getModelForClass(Person.class);
        System.out.println(OM.writeValueAsString(model));

        assertEquals("id", model.getPrimaryKeyFields()[0]);

        assertEquals(BasicField.class, model.findField("name").getClass());
        assertEquals(200, model.findBasicField("name").getColumnDefine().getCharacterMaximumLength());
        assertTrue(model.findBasicField("name").getColumnDefine().getNotNull());

        assertEquals(BasicField.class, model.findField("age").getClass());

        assertEquals(BasicField.class, model.findField("status").getClass());
        assertTrue(model.findBasicField("status").getColumnDefine().getIndex());

        assertEquals(ToOneField.class, model.findField("mainDepartment").getClass());
        assertEquals("Department", ((RefModel) model.findField("mainDepartment")).getTargetModel());

        assertEquals(ToManyField.class, model.findField("userDepartmentList").getClass());
        assertEquals("UserDepartment", ((RefModel) model.findField("userDepartmentList")).getTargetModel());

        assertEquals(BasicField.class, model.findField("type").getClass());

        assertEquals(3, ((KeyPartition) model.getTableDefine().getPartition()).getPartitionsNum());
        //assertEquals(6, ((HashPartition)model.getPartition().getSubPartition()).getPartitionsNum());
        //assertEquals("name", ((HashPartition)model.getPartition().getSubPartition()).getField());
    }

    @Test
    void getEntityModel2() throws JsonProcessingException {
        Model model = new Class2ModelTransferImpl().getModelForClass(UserAddress.class);
        System.out.println(OM.writeValueAsString(model));

        assertEquals("userId", model.getPrimaryKeyFields()[0]);
        assertEquals("addressId", model.getPrimaryKeyFields()[1]);
    }

    @Test
    void entityModelTest() throws JsonProcessingException {
        // 获取 root logger
        Logger rootLogger = (Logger) LoggerFactory.getLogger("io.github.myacelw.mybatis.dynamic");
        // 设置 root logger 的日志级别为 DEBUG
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

        ModelServiceImpl modelService = new ModelServiceBuilder(sqlSessionFactory).fillers(fillers).tablePrefix("d_").build();

        // ======== 准备 Department 模型和数据
        modelService.delete(Department.class);
        modelService.updateAndRegister(Department.class);
        DataManager<Object> departmentDataManager = modelService.getDataManager(Department.class, null);


        modelService.delete(UserDepartment.class);
        modelService.updateAndRegister(UserDepartment.class);
        DataManager<Object> userDepartmentDataManager = modelService.getDataManager(UserDepartment.class, null);


        modelService.delete(Address.class);
        modelService.updateAndRegister(Address.class);
        DataManager<Object> addressDataManager = modelService.getDataManager(Address.class, null);


        modelService.delete(UserAddress.class);
        modelService.updateAndRegister(UserAddress.class);
        DataManager<String> userAddressDataManager = modelService.getDataManager(UserAddress.class, null);


        modelService.delete(Person.class);
        Model person = modelService.getModelForClass(Person.class).addLogicDeleteFieldIfNotExist();
        //修改模型，实现字段动态扩展
        person.getFields().add(
                Field.fieldGroup("ext", Field.string("phone", 100), Field.integer("order"))
        );
        modelService.updateAndRegister(Person.class, person);
        DataManager<Object> personDataManager = modelService.getDataManager(Person.class, null);

        Department department1 = new Department();
        department1.setName("部门1");
        String department1Id = (String) departmentDataManager.insert(department1);

        Department department2 = new Department();
        department2.setName("部门2");
        String department2Id = (String) departmentDataManager.insert(department2);


        // ======== 插入 User 数据
        User user = new User();
        user.setName("zhangsan");
        user.setAge(20);
        user.setStatus(Status.Valid);

        Map<String, Object> ext = new HashMap<>();
        ext.put("phone", "13800000000");
        ext.put("order", 10);
        user.setExt(ext);

        user.setMainDepartmentId(department1.getId());

        DateBetween validDateBetween = new DateBetween();
        validDateBetween.setStartDate(LocalDate.of(2024, 1, 1));
        validDateBetween.setEndDate(LocalDate.of(2024, 12, 31));
        user.setValidDateBetween(validDateBetween);

        String userId = (String) personDataManager.insert(user);

        UserDepartment userDepartment1 = new UserDepartment();
        userDepartment1.setUserId(userId);
        userDepartment1.setDepartmentId(department1Id);
        userDepartmentDataManager.insert(userDepartment1);

        UserDepartment userDepartment2 = new UserDepartment();
        userDepartment2.setUserId(userId);
        userDepartment2.setDepartmentId(department2Id);
        userDepartmentDataManager.insert(userDepartment2);


        List<UserDepartment> userDepartmentList = userDepartmentDataManager.queryChain(UserDepartment.class).where(SimpleCondition.eq("userId", userId))
                .joins(Join.of("department"))
                .exec();
        System.out.println(OM.writeValueAsString(userDepartmentList));


        Address address1 = new Address();
        address1.setCity("北京");
        address1.setStreet("西城区景云大厦");
        addressDataManager.insert(address1);

        Address address2 = new Address();
        address2.setCity("北京");
        address2.setStreet("东城区");
        addressDataManager.insert(address2);


        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        userAddress.setAddressId(address1.getId());
        userAddressDataManager.insert(userAddress);

        UserAddress userAddress2 = new UserAddress();
        userAddress2.setUserId(userId);
        userAddress2.setAddressId(address2.getId());
        userAddressDataManager.insert(userAddress2);


        //   ====

        Guest guest = new Guest();
        guest.setName("guest");
        guest.setCode("100");

        String guestId = (String) personDataManager.insert(guest);


        boolean exists = personDataManager.exists();
        System.out.println(exists);

        // ======== User 数据 查询 1

        List<Join> joins = Arrays.asList(Join.of("mainDepartment"), Join.of("userDepartmentList.department"), Join.of("userAddressList.address"));

        List<Map<String, Object>> data = personDataManager.queryChain().joins(joins).exec();
        assertEquals(2, data.size());
        System.out.println(OM.writeValueAsString(data));

        List<Person> persons = personDataManager.queryChain(Person.class).asc("name").joins(joins).exec();
        Person p0 = persons.get(0);
        Person p1 = persons.get(1);

        assertEquals(Guest.class, p0.getClass());
        assertEquals(User.class, p1.getClass());

        // ======== User 数据 查询 2

        List<Person> data2 = personDataManager.query(Person.class, b -> b.startsWith("ext.phone", "138"));
        personDataManager.fillChain().data(data2)
                .fillField("mainDepartment")
                .fillField("userDepartmentList", b -> b.join(Join.of("department")))
                .fillField("userAddressList", b -> b.join(Join.of("address"))).exec();
        System.out.println(OM.writeValueAsString(data2));

        assertEquals(1, data2.size());
        User user2 = (User) data2.get(0);


        assertEquals("zhangsan", user2.getName());
        assertEquals(LocalDate.of(2024, 1, 1), user2.getValidDateBetween().getStartDate());
        assertEquals(LocalDate.of(2024, 12, 31), user2.getValidDateBetween().getEndDate());

        assertEquals("13800000000", data2.get(0).getExt().get("phone"));
        assertEquals(10, data2.get(0).getExt().get("order"));

        assertEquals(department1Id, user2.getMainDepartment().getId());

        assertEquals(department1Id, user2.getMainDepartment().getId());
        assertEquals("部门1", user2.getMainDepartment().getName());

        assertEquals(2, user2.getUserDepartmentList().size());
        assertTrue(user2.getUserDepartmentList().stream().allMatch(t -> t.getDepartment().getName() != null));

        assertEquals(2, user2.getUserAddressList().size());
        assertTrue(user2.getUserAddressList().stream().allMatch(t -> t.getAddress().getId() != null));
        assertTrue(user2.getUserAddressList().stream().allMatch(t -> t.getAddress().getCity() != null));

        // ======== User 数据 查询 3
        List<Person> data3 = personDataManager.queryChain(Person.class).where(SimpleCondition.startsWith("ext.phone", "138")).joins(joins).exec();
        System.out.println(OM.writeValueAsString(data3));

        assertEquals(1, data3.size());
        User user3 = (User) data3.get(0);

        assertEquals("zhangsan", data3.get(0).getName());
        assertEquals(LocalDate.of(2024, 1, 1), user3.getValidDateBetween().getStartDate());
        assertEquals(LocalDate.of(2024, 12, 31), user3.getValidDateBetween().getEndDate());

        assertEquals("13800000000", user3.getExt().get("phone"));
        assertEquals(10, data3.get(0).getExt().get("order"));

        assertEquals(department1Id, user3.getMainDepartment().getId());
        assertEquals("部门1", user3.getMainDepartment().getName());

        assertEquals(2, user3.getUserDepartmentList().size());
        assertTrue(user3.getUserDepartmentList().stream().allMatch(t -> t.getDepartment().getName() != null));

        assertEquals(2, user3.getUserAddressList().size());
        assertTrue(user3.getUserAddressList().stream().allMatch(t -> t.getAddress().getId() != null));
        assertTrue(user3.getUserAddressList().stream().allMatch(t -> t.getAddress().getCity() != null));

        // ======== 更新 User 数据
        user.setName("lisi");
        user.setAge(30);
        user.setStatus(Status.Invalid);

        ext.put("phone", "66666666");
        ext.put("order", null);
        user.setExt(ext);

        DateBetween validDateBetween2 = new DateBetween();
        validDateBetween2.setStartDate(LocalDate.of(2000, 2, 2));
        validDateBetween2.setEndDate(LocalDate.of(2020, 12, 31));
        user.setValidDateBetween(validDateBetween2);

        personDataManager.update(user);


        List<Map<String, Object>> dataUpdated = personDataManager.queryChain().joins(joins).exec();
        System.out.println(OM.writeValueAsString(dataUpdated));

        List<Person> dataUpdated2 = personDataManager.queryChain(Person.class).joins(joins).exec();
        System.out.println(OM.writeValueAsString(dataUpdated2));

    }
}