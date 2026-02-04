# mybatis-dynamic

<p align="center">
  <img src="logo.svg" alt="mybatis-dynamic logo" width="200" height="200">
</p>

[中文文档](README_CN.md)

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.myacelw/mybatis-dynamic-core.svg)](https://search.maven.org/search?q=g:io.github.myacelw%20AND%20a:mybatis-dynamic-core)

**mybatis-dynamic** is a powerful dynamic ORM framework built on top of MyBatis. It allows developers to define data models directly in Java code, automatically managing the database schema (tables, columns, indexes) and providing a rich, fluent API for CRUD operations and complex queries.

## Getting Started

### Key Features

- **Dynamic Modeling**: Define data models as Java classes. The framework automatically generates and updates the database schema (DDL) at startup or runtime.
- **Runtime Model Modification**: Models can be modified programmatically at runtime, enabling dynamic business requirements.
- **Rich Fluent API**: Construct complex queries with a natural, readable syntax. Supports automatic logical precedence (`AND` > `OR`), nested grouping, and joins.
- **Spring Boot Integration**: Seamless integration with Spring Boot. Simply annotate your model, and the framework auto-configures `BaseDao` and `BaseService` beans.
- **Advanced Mapping**: Supports `@ToOne`, `@ToMany`, and recursive relationships.
- **Visualisation**: Built-in tool to visualize model relationships.

### Architecture

The project is modularized to separate concerns:

- **`core`**: The engine. Handles dynamic modeling, SQL generation, and query execution. Can be used standalone.
- **`spring`**: Spring Boot starter. Provides auto-configuration and easy integration.
- **`draw`**: Visualization module. Provides a web UI to view entity relationships.
- **`sample`**: A complete Spring Boot sample application demonstrating usage.

### Installation & Configuration

#### 1. Requirements

- Java 8+
- Maven or Gradle
- A supported database (H2, MySQL, PostgreSQL, Oracle, OceanBase, etc.)

#### 2. Add Dependency

Add the `mybatis-dynamic-spring` dependency to your project.

**Maven:**

```xml
<dependency>
    <groupId>io.github.myacelw</groupId>
    <artifactId>mybatis-dynamic-spring</artifactId>
    <version>Latest Version</version>
</dependency>
<!-- Add your database driver, e.g., H2 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### 3. Configuration

Configure your database and `mybatis-dynamic` in `application.yml`:

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:./test;MODE=MySQL
    username: sa
    password:

mybatis-dynamic:
  # Automatically update database schema based on models
  update-model: true
  # Table name prefix
  table-prefix: t_
```

### Your First Model

Create a simple entity class annotated with `@Model`.

```java
package com.example.demo.model;

import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.annotation.IdField;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
@Model(comment = "User Table")
public class User {

    @IdField
    private Integer id;

    @BasicField(ddlComment = "User Name", ddlNotNull = true)
    private String name;

    @BasicField(ddlComment = "User Age")
    private Integer age;
}
```

### Enable Scanning

Add `@EnableModelScan` to your Spring Boot application class.

```java
@SpringBootApplication
@EnableModelScan(basePackages = "com.example.demo.model")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Basic CRUD Usage

Inject the auto-generated `BaseService` or `BaseDao` to perform operations.

```java
@RestController
@RequestMapping("/users")
public class UserController {

    // Auto-injected service for the User model
    @Autowired
    private BaseService<Integer, User> userService;

    @PostMapping
    public Integer create(@RequestBody User user) {
        return userService.insert(user);
    }

    @GetMapping
    public List<User> list() {
        // Fluent query API
        return userService.query()
                .where(c -> c.gt(User.Fields.age, 18))
                .exec();
    }
}
```

### Zero-Code REST API

The framework provides a generic `DynamicModelController` that automatically exposes REST endpoints for all registered models.

**Endpoints:**
- `GET /api/dynamic/{modelName}`: List with pagination/filtering (`?name=John&page=1&size=10`).
- `GET /api/dynamic/{modelName}/{id}`: Get by ID.
- `POST /api/dynamic/{modelName}`: Create.
- `PUT /api/dynamic/{modelName}`: Update.
- `DELETE /api/dynamic/{modelName}/{id}`: Delete.

**Configuration:**
Enabled by default. To disable:
```yaml
mybatis-dynamic:
  rest:
    enabled: false
```

## Advanced Data Management

### Advanced Entity Modeling

#### Annotations Reference

- **`@Model`**: Marks a class as a managed model.
  - `tableName`: Custom table name (default: derived from class name).
  - `comment`: Database table comment.
  - `logicDelete`: Enable logical deletion.
  - `disableTableCreateAndAlter`: Disable auto-DDL.

- **`@IdField`**: Marks the primary key field.
  - `keyGeneratorMode`: ID generation strategy.
  - `order`: **Required for Composite Primary Keys**.
  - `ddlColumnType`: Manually specify column type (e.g., "VARCHAR(64)").

- **`@BasicField`**: Maps a field to a standard database column.
  - `columnName`: Custom column name.
  - `ddlNotNull`: Column cannot be null.
  - `ddlDefaultValue`: Default value definition.
  - `ddlCharacterMaximumLength`: Max length for string columns.
  - `ddlNumericPrecision` / `ddlNumericScale`: Precision for numeric types.
  - `ddlIndex`: Create an index.
  - `ddlIndexType`: Type of index (`NORMAL`, `UNIQUE`).
  - `ddlComment`: Column comment.

- **`@ToOne`**: Defines a "Many-to-One" or "One-to-One" relationship.
  - `targetModel`: Target model name (optional if field type is a Model).
  - `joinField`: Foreign key field in *this* model (default: `{fieldName}Id`).

- **`@ToMany`**: Defines a "One-to-Many" or "Many-to-Many" relationship.
  - `targetModel`: Target model name (optional if List generic type is a Model).
  - `joinField`: Foreign key field in the *target* model (default: `{thisModelName}Id`).

- **`@IgnoreField`**: Excludes the field from database mapping.

#### Relationships & Querying

**A. One-to-One / Many-to-One**

*Example: A User belongs to a Department.*

```java
@Data
@Model
public class User {
    @IdField
    private String id;
    private String name;
    
    // Defines relation to Department. 
    // Expects "departmentId" column in User table.
    @ToOne 
    private Department department;
}
```

**Querying (Auto-Join):**
Selecting a field from a related entity automatically triggers a **Left Join**.

```java
// Automatically joins 'department' table to fetch department name
List<User> users = userService.query()
        .select(User.Fields.name, "department.name")
        .exec();
```

**Explicit Join Configuration:**
You can customize the join type (e.g., INNER JOIN) and add ON conditions using the `on()` method.

```java
// Customize join type and add conditions
List<User> users = userService.query()
        .joins(Join.inner("mainDepartment")
                   .on(c -> c.eq("active", true)))
        .exec();
```

**B. One-to-Many**

*Example: A Department has many Users.*

```java
@Data
@Model
public class Department {
    @IdField
    private String id;
    private String name;
    
    // Defines relation to Users.
    // Expects "departmentId" column in User table.
    @ToMany 
    private List<User> users;
}
```

**Querying:**
```java
// Fetch Department and all its Users
List<Department> depts = departmentService.query()
        .joins(Join.of("users"))
        .exec();
```

**C. Many-to-Many**

Implemented using an **Intermediate Entity** with a composite primary key.

*Example: Students and Courses.*

```java
// 1. Student
@Model
public class Student {
    @IdField private String id;
    
    // Relation to the intermediate table
    @ToMany 
    private List<StudentCourse> studentCourses;
}

// 2. Course
@Model
public class Course {
    @IdField private String id;
}

// 3. StudentCourse (Intermediate)
@Model
public class StudentCourse {
    @IdField(order = 0) private String studentId;
    @IdField(order = 1) private String courseId;

    @ToOne private Course course;
}
```

**Querying:**
```java
// Fetch Student and their Courses (joining through StudentCourse)
List<Student> students = studentService.query()
        .joins(Join.of("studentCourses.course")) 
        .exec();
```

#### Composite Primary Keys

Annotate multiple fields with `@IdField` and specify their `order`.

```java
@Model
public class UserRole {
    @IdField(order = 0)
    private String userId;

    @IdField(order = 1)
    private String roleId;
}
```

#### Inheritance (Single Table)

Map an inheritance hierarchy to a single database table.

```java
@Model(tableName = "person")
@SubTypes(subTypes = {@SubTypes.SubType(User.class), @SubTypes.SubType(Guest.class)}, subTypeFieldName = "type")
public abstract class Person {
    @IdField
    private String id;
    private String name;
}

@Data
public class User extends Person {
    private Integer age;
}

@Data
public class Guest extends Person {
    private String token;
}
```

#### Extensibility (`ExtBean`)

Handle dynamic fields that are not explicitly defined in the Java class.

```java
@Data
@Model
public class User implements ExtBean {
    @IdField
    private String id;
    private String name;

    @IgnoreField
    private Map<String, Object> ext = new HashMap<>();

    @Override
    public Map<String, Object> getExt() {
        return ext;
    }
}
```

**Usage:**
```java
// 1. Add dynamic field definition at runtime
Model userModel = modelService.getModelForClass(User.class);
userModel.getFields().add(Field.string("phone", 100));
modelService.update(userModel);

// 2. Use it (Insert/Update/Query)
User user = new User();
user.getExt().put("phone", "123456");
userService.insert(user);

List<User> users = userService.query()
        .where(c -> c.eq("phone", "123456"))
        .exec();
```

### Fluent Query API

Construct complex queries with automatic logical precedence (`AND` > `OR`).

#### 1. Condition Operators

The `ConditionBuilder` supports a wide range of operators:

- **Simple**: `eq` (equal), `ne` (not equal), `gt` (greater than), `gte`, `lt`, `lte`.
- **String**: `like`, `startsWith`, `endsWith`, `contains`.
- **Null Check**: `isNull`, `isNotNull`, `isBlank`, `isNotBlank`.
- **Collection**: `in`, `notIn`.
- **Logical**: `and`, `or`, `not` (nested).
- **Exists**: `exists` (Subquery).

**Optional Variants:**
All the above operators (except Null Check and Exists) have an `Optional` variant (e.g., `eqOptional`, `likeOptional`, `inOptional`).
These methods automatically ignore the condition if the provided value is `null`, an empty string, or an empty collection.

```java
// If name is null, "name = ?" is not added to SQL.
// If age is null, "age > ?" is not added to SQL.
userService.query()
    .where(c -> c.eqOptional("name", name)
                 .gtOptional("age", age))
    .exec();
```

#### 2. Examples

**Complex Logic**
```java
// WHERE (age > 18 AND status = 'Active') OR role = 'Admin'
userService.query()
        .where(c -> c.bracket(b -> b.gt("age", 18).eq("status", "Active"))
                     .or(b -> b.eq("role", "Admin")))
        .joins(Join.of("mainDepartment")) 
        .exec();
```

**Exists Subquery**
```java
// Find users who have at least one order with amount > 100
userService.query()
    .where(c -> c.exists("orders", sub -> sub.gt("amount", 100)))
    .exec();
```

### DataManager with Maps

The `DataManager` interface supports `Map<String, Object>` for scenarios without entity classes.

```java
// Get DataManager
DataManager<Integer> dataManager = modelService.getDataManager("User");

// Insert Map
Map<String, Object> data = new HashMap<>();
data.put("name", "Bob");
Integer id = dataManager.insert(data);

// Query returning Maps
List<Map<String, Object>> results = dataManager.query()
        .where(c -> c.gt("age", 20))
        .exec();
```

### Batch Operations

Efficiently handle large datasets.

```java
List<User> userList = ...;

// Batch Insert
userService.getDataManager().batchInsert(userList);

// Batch Update (by ID)
userService.getDataManager().batchUpdate(userList);

// Batch Update by Condition
userService.batchUpdateByConditionChain()
        .add(c -> c.eq("status", "Active"), user1)
        .add(c -> c.eq("status", "Inactive"), user2)
        .exec();
```

### Aggregation Queries

Perform SQL aggregate functions (COUNT, SUM, AVG, MAX, MIN) with grouping.

#### 1. Basic Usage

Use `dataManager.aggQuery()` to start an aggregation query chain.

```java
// Count all users
Map<String, Object> result = userService.getDataManager()
    .aggQuery()
    .count() // Default: COUNT(*), alias "count"
    .exec()
    .get(0);
Long count = (Long) result.get("count");
```

#### 2. Aggregation Functions & Grouping

You can specify the field, alias, and grouping.

```java
// Calculate average age and max age per department
// SQL: SELECT department_id as deptId, AVG(age) as avgAge, MAX(age) as maxAge FROM user GROUP BY department_id
List<Map<String, Object>> results = userService.getDataManager()
    .aggQuery()
    .groupBy("departmentId", "deptId")
    .avg("age", "avgAge")
    .max("age", "maxAge")
    .exec();
```

#### 3. Filtering

Use `.where()` to filter data *before* aggregation.

```java
// Count users older than 18
List<Map<String, Object>> results = userService.getDataManager()
    .aggQuery()
    .count()
    .where(c -> c.gt("age", 18))
    .exec();
```

#### 4. Result Mapping

Map results to a DTO class.

```java
@Data
public class DeptStats {
    private String deptId;
    private Double avgAge;
}

List<DeptStats> stats = userService.getDataManager()
    .aggQuery(DeptStats.class)
    .groupBy("departmentId", "deptId")
    .avg("age", "avgAge")
    .exec();
```

### Recursive Queries

Retrieve hierarchical data (e.g., Department Tree).

```java
Map<String, Object> tree = departmentService.getRecursiveTreeById("dept-id");
```

## Expert Features

### Interceptors

Hook into data operations (`beforeInsert`, `afterUpdate`, etc.).

```java
@Component
public class MyInterceptor implements DataChangeInterceptor {
    @Override
    public void beforeInsert(DataManager<Object> dataManager, Object data) {
        // ...
    }
}
```

### Auto-Fillers

Automatically populate fields (e.g., `createTime`, `updateUser`). Implement `Filler` or extend `AbstractCreatorFiller`.

### Permission Management

The framework provides a robust permission system to control access to data (Row Permissions) and fields (Column Permissions).

#### 1. Concepts

- **Row Permissions (Data Rights)**: Filters data based on conditions (e.g., `tenant_id = 'T001'`). Applied automatically to Select, Update, and Delete operations.
- **Column Permissions (Field Rights)**: Restricts which fields are visible (in Select) or modifiable (in Insert/Update).

#### 2. Implementing Permissions

Implement the `CurrentUserHolder` interface to provide user context and permissions.

```java
@Component
public class MyUserHolder implements CurrentUserHolder {

    @Override
    public String getCurrentUserId() {
        // Return current user ID from Security Context (e.g., Spring Security)
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public Permission getCurrentUserPermission(Model model) {
        // Return permissions based on the model and current user
        if ("User".equals(model.getName())) {
            // Row Permission: Only see users in the same tenant
            Condition dataRights = SimpleCondition.eq("tenant_id", getCurrentTenantId());
            
            // Column Permission: Cannot see "password" or "salary"
            // If list is null, all fields are accessible.
            List<String> fieldRights = Arrays.asList("id", "name", "age", "tenant_id");
            
            return new Permission(fieldRights, dataRights);
        }
        return null; // No restrictions
    }
}
```

#### 3. Multi-Tenancy

Multi-tenancy is a primary use case for Row Permissions. By returning a `dataRights` condition (e.g., `tenant_id = current_tenant_id`) in `getCurrentUserPermission`, you ensure strict data isolation across the application.

### Custom Type Handlers

**1. Runtime Data Mapping (MyBatis TypeHandler)**

Use standard MyBatis `TypeHandler` to convert data between Java and Database at runtime.

```java
@BasicField(typeHandler = MyJsonTypeHandler.class)
private MyObject data;
```

**2. DDL Type Mapping (ColumnTypeHandler)**

Control how a Java type maps to a Database Column Definition (e.g., `VARCHAR(255)`) during auto-DDL.

```java
@Component
public class MyCustomTypeHandler implements ColumnTypeHandler {
    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, Field field) {
        if (javaType == MyCustomType.class) {
            column.setDataType("VARCHAR");
            column.setCharacterMaximumLength(500);
            return true; // Handled
        }
        return false;
    }
}
```

## Community

- **Issues**: Please file an issue for bugs or feature requests.
- **Discussions**: Join the discussions on GitHub.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
