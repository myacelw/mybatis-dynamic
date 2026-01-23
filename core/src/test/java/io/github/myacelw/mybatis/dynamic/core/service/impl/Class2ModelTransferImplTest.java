package io.github.myacelw.mybatis.dynamic.core.service.impl;

import io.github.myacelw.mybatis.dynamic.core.annotation.*;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.GroupField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.ToManyField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.ToOneField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Class2ModelTransferImplTest {

    private Class2ModelTransferImpl transfer;

    @BeforeEach
    void setUp() {
        transfer = new Class2ModelTransferImpl();
    }

    @Test
    void getModelForClass_BasicFields() {
        Model model = transfer.getModelForClass(SimpleUser.class);

        assertEquals("User", model.getName());
        assertEquals(SimpleUser.class, model.getJavaType());
        
        List<Field> fields = model.getFields();
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("id")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("name")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("age")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("active")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("type")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("extra")));

        Field idField = model.findField("id");
        assertTrue(model.isPrimaryKeyField("id"));
        assertEquals(String.class, idField.getJavaClass());

        Field typeField = model.findField("type");
        assertEquals(UserType.class, typeField.getJavaClass());
    }

    @Test
    void getModelForClass_Relationships() {
        Model model = transfer.getModelForClass(Order.class);

        Field userField = model.findField("user");
        assertTrue(userField instanceof ToOneField);
        assertEquals("User", ((ToOneField) userField).getTargetModel());

        Field itemsField = model.findField("items");
        assertTrue(itemsField instanceof ToManyField);
        assertEquals("OrderItem", ((ToManyField) itemsField).getTargetModel());
        assertEquals(OrderItem.class, itemsField.getJavaClass());
    }

    @Test
    void getModelForClass_InheritanceAndGroup() {
        Model model = transfer.getModelForClass(Employee.class);

        assertNotNull(model.findField("id"));
        assertNotNull(model.findField("createTime"));
        assertNotNull(model.findField("name"));

        Field addrField = model.findField("address");
        assertTrue(addrField instanceof GroupField);
        GroupField groupField = (GroupField) addrField;
        
        List<BasicField> subFields = groupField.getFields();
        assertEquals(2, subFields.size());
        
        BasicField cityField = subFields.stream().filter(f -> f.getName().equals("city")).findFirst().orElse(null);
        assertNotNull(cityField);
        assertEquals("addr_city", cityField.getColumnName());
    }

    @Test
    void getModelForClass_Annotations() {
        Model model = transfer.getModelForClass(AnnotatedUser.class);

        BasicField secretField = (BasicField) model.findField("secret");
        assertNotNull(secretField);
        assertEquals("u_secret", secretField.getColumnName());
        assertEquals(Boolean.FALSE, secretField.getSelect());
        assertEquals(MySecretTypeHandler.class, secretField.getTypeHandlerClass());

        BasicField nameField = (BasicField) model.findField("name");
        assertEquals(100, nameField.getColumnDefinition().getCharacterMaximumLength());
        assertTrue(nameField.getColumnDefinition().getNotNull());
    }

    @Test
    void getModelForClass_SubTypes() {
        Model model = transfer.getModelForClass(Vehicle.class);

        assertEquals("v_type", model.getExtProperties().get(Model.EXT_PROPERTY_SUB_TYPE_FIELD_NAME));
        Map<String, Class<?>> subTypeMap = (Map<String, Class<?>>) model.getExtProperties().get(Model.EXT_PROPERTY_SUB_TYPE_MAP);
        assertNotNull(subTypeMap);
        assertEquals(Car.class, subTypeMap.get("CAR"));
        assertEquals(Truck.class, subTypeMap.get("TRUCK"));

        assertNotNull(model.findField("numberOfDoors"));
        assertNotNull(model.findField("loadCapacity"));
        assertNotNull(model.findField("brand"));
    }

    @Test
    void getModelForClass_LogicDelete() {
        Model model = transfer.getModelForClass(DeletedUser.class);
        
        Field deleteFlag = model.findField(Model.FIELD_DELETE_FLAG);
        assertNotNull(deleteFlag);
        assertTrue(deleteFlag instanceof BasicField);
    }

    @Test
    void getModelForClass_ExtProperties() {
        Model model = transfer.getModelForClass(SimpleUser.class);
        assertEquals("io.github.myacelw.mybatis.dynamic.core.service.impl", model.getExtProperties().get(Model.EXT_PROPERTY_MODULE_GROUP));
    }

    @Test
    void getModelForClass_FieldSorting() {
        Model model = transfer.getModelForClass(SimpleUser.class);
        List<Field> fields = model.getFields();
        assertEquals("id", fields.get(0).getName(), "ID should be the first field");
    }

    // --- Test Models ---

    @io.github.myacelw.mybatis.dynamic.core.annotation.Model(name = "User")
    public static class SimpleUser {
        @IdField
        private String id;
        private String name;
        private int age;
        private Boolean active;
        private UserType type;
        private Map<String, Object> extra;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public UserType getType() { return type; }
        public void setType(UserType type) { this.type = type; }
        public Map<String, Object> getExtra() { return extra; }
        public void setExtra(Map<String, Object> extra) { this.extra = extra; }
    }

    public enum UserType { ADMIN, GUEST }

    @io.github.myacelw.mybatis.dynamic.core.annotation.Model(name = "Order")
    public static class Order {
        @IdField
        private String id;
        @ToOne(targetModel = "User")
        private SimpleUser user;
        @ToMany(targetModel = "OrderItem")
        private List<OrderItem> items;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public SimpleUser getUser() { return user; }
        public void setUser(SimpleUser user) { this.user = user; }
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }
    }

    @io.github.myacelw.mybatis.dynamic.core.annotation.Model(name = "OrderItem")
    public static class OrderItem {
        @IdField
        private String id;
        private String productName;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
    }

    public static class BaseEntity {
        @IdField(order = -1)
        private String id;
        private Long createTime;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Long getCreateTime() { return createTime; }
        public void setCreateTime(Long createTime) { this.createTime = createTime; }
    }

    @io.github.myacelw.mybatis.dynamic.core.annotation.Model(name = "Employee")
    public static class Employee extends BaseEntity {
        private String name;
        @io.github.myacelw.mybatis.dynamic.core.annotation.GroupField(columnPrefix = "addr_")
        private Address address;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }
    }

    public static class Address {
        private String city;
        private String street;

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
    }

    @io.github.myacelw.mybatis.dynamic.core.annotation.Model(name = "AnnotatedUser")
    public static class AnnotatedUser {
        @IdField
        private String id;
        
        @io.github.myacelw.mybatis.dynamic.core.annotation.BasicField(columnName = "u_secret", select = false, typeHandler = MySecretTypeHandler.class)
        private String secret;

        @io.github.myacelw.mybatis.dynamic.core.annotation.BasicField(ddlCharacterMaximumLength = 100, ddlNotNull = true)
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @SubTypes(subTypeFieldName = "v_type", subTypes = {
            @SubTypes.SubType(name = "CAR", value = Car.class),
            @SubTypes.SubType(name = "TRUCK", value = Truck.class)
    })
    @io.github.myacelw.mybatis.dynamic.core.annotation.Model(name = "Vehicle")
    public static class Vehicle {
        @IdField
        private String id;
        private String brand;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
    }

    public static class Car extends Vehicle {
        private int numberOfDoors;
        public int getNumberOfDoors() { return numberOfDoors; }
        public void setNumberOfDoors(int numberOfDoors) { this.numberOfDoors = numberOfDoors; }
    }

    public static class Truck extends Vehicle {
        private double loadCapacity;
        public double getLoadCapacity() { return loadCapacity; }
        public void setLoadCapacity(double loadCapacity) { this.loadCapacity = loadCapacity; }
    }

    @io.github.myacelw.mybatis.dynamic.core.annotation.Model(logicDelete = true)
    public static class DeletedUser {
        @IdField
        private String id;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }
}