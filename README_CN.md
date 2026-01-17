# mybatis-dynamic

[English](README.md)

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.myacelw/mybatis-dynamic-core.svg)](https://search.maven.org/search?q=g:io.github.myacelw%20AND%20a:mybatis-dynamic-core)

**mybatis-dynamic** 是一个基于 MyBatis 构建的强大动态 ORM 框架。它允许开发者直接在 Java 代码中定义数据模型，自动管理数据库架构（表、列、索引），并提供丰富流畅的 API 用于 CRUD 操作和复杂查询。

## 快速入门 (Getting Started)

### 主要特性

- **动态建模**: 将数据模型定义为 Java 类。框架会在启动或运行时自动生成和更新数据库架构 (DDL)。
- **运行时模型修改**: 支持在运行时通过编程方式修改模型，适应动态变化的业务需求。
- **丰富流畅的 API**: 使用自然、易读的语法构建复杂查询。支持自动逻辑优先级 (`AND` > `OR`)、嵌套分组和连表查询。
- **Spring Boot 集成**: 与 Spring Boot 无缝集成。只需注解您的模型，框架即可自动配置 `BaseDao` 和 `BaseService` bean。
- **高级映射**: 支持 `@ToOne`、`@ToMany` 和递归关系。
- **可视化**: 内置工具用于可视化模型关系。

### 架构

项目采用模块化设计，分离关注点：

- **`core`**: 核心引擎。处理动态建模、SQL 生成和查询执行。可独立使用。
- **`spring`**: Spring Boot 启动器。提供自动配置和便捷集成。
- **`draw`**: 可视化模块。提供 Web UI 以查看实体关系。
- **`sample`**: 一个完整的 Spring Boot 示例应用程序，演示了用法。

### 安装与配置

#### 1. 环境要求

- Java 8+
- Maven 或 Gradle
- 支持的数据库 (H2, MySQL, PostgreSQL, Oracle, OceanBase 等)

#### 2. 添加依赖

将 `mybatis-dynamic-spring` 依赖添加到您的项目中。

**Maven:**

```xml
<dependency>
    <groupId>io.github.myacelw</groupId>
    <artifactId>mybatis-dynamic-spring</artifactId>
    <version>最新版本</version>
</dependency>
<!-- 添加您的数据库驱动，例如 H2 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### 3. 配置

在 `application.yml` 中配置数据库和 `mybatis-dynamic`：

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:./test;MODE=MySQL
    username: sa
    password:

mybatis-dynamic:
  # 根据模型自动更新数据库架构
  update-model: true
  # 表名前缀
  table-prefix: t_
```

### 第一个模型

创建一个带有 `@Model` 注解的简单实体类。

```java
package com.example.demo.model;

import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.annotation.IdField;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
@Model(comment = "用户表")
public class User {

    @IdField
    private Integer id;

    @BasicField(ddlComment = "用户名", ddlNotNull = true)
    private String name;

    @BasicField(ddlComment = "用户年龄")
    private Integer age;
}
```

### 启用扫描

在您的 Spring Boot 应用类上添加 `@EnableModelScan`。

```java
@SpringBootApplication
@EnableModelScan(basePackages = "com.example.demo.model")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 基础 CRUD 操作

注入自动生成的 `BaseService` 或 `BaseDao` 来执行操作。

```java
@RestController
@RequestMapping("/users")
public class UserController {

    // 自动注入 User 模型的 Service
    @Autowired
    private BaseService<Integer, User> userService;

    @PostMapping
    public Integer create(@RequestBody User user) {
        return userService.insert(user);
    }

    @GetMapping
    public List<User> list() {
        // 流畅查询 API
        return userService.queryChain()
                .where(c -> c.gt(User.Fields.age, 18))
                .exec();
    }
}
```

### 零代码 REST API

框架提供了一个通用的 `DynamicModelController`，自动为所有已注册的模型暴露 REST 端点。

**端点 (Endpoints):**
- `GET /api/dynamic/{modelName}`: 列表查询，支持分页/过滤 (`?name=John&page=1&size=10`)。
- `GET /api/dynamic/{modelName}/{id}`: 根据 ID 获取。
- `POST /api/dynamic/{modelName}`: 创建。
- `PUT /api/dynamic/{modelName}`: 更新。
- `DELETE /api/dynamic/{modelName}/{id}`: 删除。

**配置 (Configuration):**
默认启用。禁用方式：
```yaml
mybatis-dynamic:
  rest:
    enabled: false
```

## 进阶数据管理 (Advanced Data Management)

### 高级实体建模

#### 1. 注解参考

- **`@Model`**: 将类标记为托管模型。
  - `tableName`: 自定义表名（默认：类名驼峰转下划线，例如 `UserProfile` -> `user_profile`）。
  - `comment`: 数据库表注释。
  - `logicDelete`: 启用逻辑删除（需要一个 `Integer` 类型的删除标记字段）。
  - `disableTableCreateAndAlter`: 禁用此特定模型的自动 DDL。

- **`@IdField`**: 标记主键字段。
  - `keyGeneratorMode`: ID 生成策略（例如 `UUID`, `AUTO`, `SNOWFLAKE`）。
  - `order`: **复合主键必须**。指定键的顺序（0, 1, ...）。
  - `ddlColumnType`: 手动指定列类型名称（例如 `VARCHAR`）。注意：在 `ddlCharacterMaximumLength` 中指定长度。

- **`@BasicField`**: 将字段映射到标准的数据库列。
  - `columnName`: 自定义列名。
  - `ddlNotNull`: 列不能为空（默认：`false`）。
  - `ddlDefaultValue`: 默认值定义（例如 `0`, `'active'`）。
  - `ddlCharacterMaximumLength`: 字符串列的最大长度。
  - `ddlNumericPrecision` / `ddlNumericScale`: 数值类型的精度。
  - `ddlIndex`: 创建索引（默认：`false`）。
  - `ddlIndexType`: 索引类型 (`NORMAL`, `UNIQUE`)。
  - `ddlComment`: 列注释。

- **`@ToOne`**: 定义“多对一”或“一对一”关系。
  - `targetModel`: 目标模型名称（如果字段类型是模型类，则可选）。
  - `joinField`: 当前模型中的外键字段（默认：`{fieldName}Id`）。

- **`@ToMany`**: 定义“一对多”或“多对多”关系。
  - `targetModel`: 目标模型名称（如果 List 泛型类型是模型类，则可选）。
  - `joinField`: 目标模型中的外键字段（默认：`{thisModelName}Id`）。

- **`@IgnoreField`**: 将字段排除在数据库映射之外。

#### 2. 关系与查询

使用注解定义关系，并使用 `.joins()` 进行查询。

**A. 一对一 / 多对一**

*示例：一个用户属于一个部门。*

```java
@Data
@Model
public class User {
    @IdField
    private String id;
    private String name;
    
    // 定义到 Department 的关系。
    // 期望 User 表中存在 "departmentId" 列。
    @ToOne 
    private Department department;
}
```

**查询 (自动 Join):**
当选择关联实体的字段时，框架会自动触发 **左连接 (Left Join)**。

```java
// 自动关联 'department' 表以获取部门名称
List<User> users = userService.queryChain()
        .select(User.Fields.name, "department.name")
        .exec();
```

**显式 Join 配置:**
您可以自定义连接类型（例如 INNER JOIN）并添加 ON 条件。

```java
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.JoinType;

List<User> users = userService.queryChain()
        .joins(Join.of("department", JoinType.INNER)
                   .and(c -> c.eq("department.active", true)))
        .exec();
```

**B. 一对多**

*示例：一个部门有多个用户。*

```java
@Data
@Model
public class Department {
    @IdField
    private String id;
    private String name;
    
    // 定义到 Users 的关系。
    // 期望 User 表中存在 "departmentId" 列。
    @ToMany 
    private List<User> users;
}
```

**查询:**
```java
// 获取部门及其所有用户
List<Department> depts = departmentService.queryChain()
        .joins(Join.of("users"))
        .exec();
```

**C. 多对多**

通过具有复合主键的**中间实体**实现。

*示例：学生和课程。*

```java
// 1. 学生
@Model
public class Student {
    @IdField private String id;
    
    // 关联到中间表
    @ToMany 
    private List<StudentCourse> studentCourses;
}

// 2. 课程
@Model
public class Course {
    @IdField private String id;
}

// 3. 学生选课 (中间实体)
@Model
public class StudentCourse {
    @IdField(order = 0) private String studentId;
    @IdField(order = 1) private String courseId;

    @ToOne private Course course;
}
```

**查询:**
```java
// 获取学生及其课程 (通过 StudentCourse 关联)
List<Student> students = studentService.queryChain()
        .joins(Join.of("studentCourses.course")) 
        .exec();
```

#### 3. 复合主键

使用 `@IdField` 注解多个字段并指定它们的 `order`。

```java
@Model
public class UserRole {
    @IdField(order = 0)
    private String userId;

    @IdField(order = 1)
    private String roleId;
}
```

#### 4. 继承 (单表)

将继承层次结构映射到单张数据库表。

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

#### 5. 扩展性 (`ExtBean`)

处理未在 Java 类中显式定义的动态字段。

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

**使用:**
```java
// 1. 运行时添加动态字段定义
Model userModel = modelService.getModelForClass(User.class);
userModel.getFields().add(Field.string("phone", 100));
modelService.update(userModel);

// 2. 使用它 (插入/更新/查询)
User user = new User();
user.getExt().put("phone", "123456");
userService.insert(user);

List<User> users = userService.queryChain()
        .where(c -> c.eq("phone", "123456"))
        .exec();
```

### 流畅查询 API (Fluent Query API)

构建具有自动逻辑优先级 (`AND` > `OR`) 的复杂查询。

#### 1. 条件操作符

`ConditionBuilder` 支持广泛的操作符：

- **简单**: `eq` (等于), `ne` (不等于), `gt` (大于), `gte`, `lt`, `lte`.
- **字符串**: `like`, `startsWith` (前缀), `endsWith` (后缀), `contains` (包含).
- **空值检查**: `isNull`, `isNotNull`, `isBlank`, `isNotBlank`.
- **集合**: `in`, `notIn`.
- **逻辑**: `and`, `or`, `not` (嵌套).
- **存在**: `exists` (子查询).

**可选变体 (Optional Variants):**
上述大多数操作符（空值检查和 Exists 除外）都有一个 `Optional` 变体（例如 `eqOptional`, `likeOptional`, `inOptional`）。
如果传入的值为 `null`、空字符串或空集合，这些方法会自动忽略该条件，不将其添加到最终的 SQL 中。

```java
// 如果 name 为 null，则不会将 "name = ?" 添加到 SQL 中。
// 如果 age 为 null，则不会将 "age > ?" 添加到 SQL 中。
userService.queryChain()
    .where(c -> c.eqOptional("name", name)
                 .gtOptional("age", age))
    .exec();
```

#### 2. 示例

**复杂逻辑**
```java
// WHERE (age > 18 AND status = 'Active') OR role = 'Admin'
userService.queryChain()
        .where(c -> c.bracket(b -> b.gt("age", 18).eq("status", "Active"))
                     .or(b -> b.eq("role", "Admin")))
        .exec();
```

**Exists 子查询**
```java
// 查找至少有一笔订单金额大于 100 的用户
userService.queryChain()
    .where(c -> c.exists("orders", sub -> sub.gt("amount", 100)))
    .exec();
```

### 使用 Map 的 DataManager

`DataManager` 接口支持 `Map<String, Object>`，适用于没有实体类的场景。

```java
// 获取 DataManager
DataManager<Integer> dataManager = modelService.getDataManager("User");

// 插入 Map
Map<String, Object> data = new HashMap<>();
data.put("name", "Bob");
Integer id = dataManager.insert(data);

// 返回 Map 的查询
List<Map<String, Object>> results = dataManager.queryChain()
        .where(c -> c.gt("age", 20))
        .exec();
```

### 批量操作 (Batch Operations)

高效处理大量数据。

```java
List<User> userList = ...;

// 批量插入
userService.getDataManager().batchInsert(userList);

// 批量更新 (根据 ID)
userService.getDataManager().batchUpdate(userList);

// 按条件批量更新
userService.batchUpdateByConditionChain()
        .add(c -> c.eq("status", "Active"), user1)
        .add(c -> c.eq("status", "Inactive"), user2)
        .exec();
```

### 聚合查询 (Aggregation Queries)

执行带有分组的 SQL 聚合函数 (COUNT, SUM, AVG, MAX, MIN)。

#### 1. 基本用法

使用 `dataManager.aggQuery()` 开启聚合查询链。

```java
// 统计所有用户
Map<String, Object> result = userService.getDataManager()
    .aggQuery()
    .count() // 默认: COUNT(*), 别名 "count"
    .exec()
    .get(0);
Long count = (Long) result.get("count");
```

#### 2. 聚合函数与分组

指定字段、别名和分组。

```java
// 计算每个部门的平均年龄和最大年龄
// SQL: SELECT department_id as deptId, AVG(age) as avgAge, MAX(age) as maxAge FROM user GROUP BY department_id
List<Map<String, Object>> results = userService.getDataManager()
    .aggQuery()
    .groupBy("departmentId", "deptId")
    .avg("age", "avgAge")
    .max("age", "maxAge")
    .exec();
```

#### 3. 过滤

使用 `.where()` 在聚合*之前*过滤数据。

```java
// 统计大于 18 岁的用户
List<Map<String, Object>> results = userService.getDataManager()
    .aggQuery()
    .count()
    .where(c -> c.gt("age", 18))
    .exec();
```

#### 4. 结果映射

将结果映射到 DTO 类。

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

### 递归查询

检索层次结构数据（例如部门树）。

```java
Map<String, Object> tree = departmentService.getRecursiveTreeById("dept-id");
```

## 专家功能 (Expert Features)

### 拦截器 (Interceptors)

挂钩数据操作（`beforeInsert`, `afterUpdate` 等）。

```java
@Component
public class MyInterceptor implements DataChangeInterceptor {
    @Override
    public void beforeInsert(DataManager<Object> dataManager, Object data) {
        // ...
    }
}
```

### 自动填充 (Auto-Fillers)

自动填充字段（例如 `createTime`, `updateUser`）。实现 `Filler` 接口或继承 `AbstractCreatorFiller`。

### 权限管理 (Permission Management)

框架提供了强大的权限系统来控制对数据（行权限）和字段（列权限）的访问。

#### 1. 概念

- **行权限 (Data Rights)**: 基于条件过滤数据（例如 `tenant_id = 'T001'`）。自动应用于 Select、Update 和 Delete 操作。
- **列权限 (Field Rights)**: 限制哪些字段可见（在 Select 中）或可修改（在 Insert/Update 中）。

#### 2. 实现权限

实现 `CurrentUserHolder` 接口以提供用户上下文和权限。

```java
@Component
public class MyUserHolder implements CurrentUserHolder {

    @Override
    public String getCurrentUserId() {
        // 从安全上下文（如 Spring Security）返回当前用户 ID
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public Permission getCurrentUserPermission(Model model) {
        // 根据模型和当前用户返回权限
        if ("User".equals(model.getName())) {
            // 行权限：只能查看同一租户下的用户
            Condition dataRights = SimpleCondition.eq("tenant_id", getCurrentTenantId());
            
            // 列权限：不能查看 "password" 或 "salary"
            // 如果列表为 null，则所有字段均可访问。
            List<String> fieldRights = Arrays.asList("id", "name", "age", "tenant_id");
            
            return new Permission(fieldRights, dataRights);
        }
        return null; // 无限制
    }
}
```

#### 3. 多租户 (Multi-Tenancy)

多租户是行权限的一个主要用例。通过在 `getCurrentUserPermission` 中返回一个 `dataRights` 条件（例如 `tenant_id = current_tenant_id`），您可以确保应用程序中严格的数据隔离。

### 自定义类型处理器 (Custom Type Handlers)

**1. 运行时数据映射 (MyBatis TypeHandler)**

使用标准的 MyBatis `TypeHandler` 在运行时转换 Java 和数据库之间的数据。

```java
@BasicField(typeHandler = MyJsonTypeHandler.class)
private MyObject data;
```

**2. DDL 类型映射 (ColumnTypeHandler)**

控制 Java 类型如何在自动 DDL 期间映射到数据库列定义（例如 `VARCHAR(255)`）。

```java
@Component
public class MyCustomTypeHandler implements ColumnTypeHandler {
    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, Field field) {
        if (javaType == MyCustomType.class) {
            column.setDataType("VARCHAR");
            column.setCharacterMaximumLength(500);
            return true; // 已处理
        }
        return false;
    }
}
```

## 社区

- **Issues**: 提交 Bug 或功能建议。
- **Discussions**: 参与 GitHub 讨论。

## 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。
