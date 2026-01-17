# mybatis-dynamic

[中文文档](README_CN.md)

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.myacelw/mybatis-dynamic-core.svg)](https://search.maven.org/search?q=g:io.github.myacelw%20AND%20a:mybatis-dynamic-core)

**mybatis-dynamic** is a powerful dynamic ORM framework built on top of MyBatis. It allows developers to define data models directly in Java code, automatically managing the database schema (tables, columns, indexes) and providing a rich, fluent API for CRUD operations and complex queries.

## Key Features

- **Dynamic Modeling**: Define data models as Java classes. The framework automatically generates and updates the database schema (DDL) at startup or runtime.
- **Runtime Model Modification**: Models can be modified programmatically at runtime, enabling dynamic business requirements.
- **Rich Fluent API**: Construct complex queries with a natural, readable syntax. Supports automatic logical precedence (`AND` > `OR`), nested grouping, and joins.
- **Spring Boot Integration**: Seamless integration with Spring Boot. Simply annotate your model, and the framework auto-configures `BaseDao` and `BaseService` beans.
- **Advanced Mapping**: Supports `@ToOne`, `@ToMany`, and recursive relationships.
- **Visualisation**: Built-in tool to visualize model relationships.

## Architecture

The project is modularized to separate concerns:

- **`core`**: The engine. Handles dynamic modeling, SQL generation, and query execution. Can be used standalone.
- **`spring`**: Spring Boot starter. Provides auto-configuration and easy integration.
- **`draw`**: Visualization module. Provides a web UI to view entity relationships.
- **`sample`**: A complete Spring Boot sample application demonstrating usage.

## Quick Start

### 1. Requirements

- Java 8+
- Maven or Gradle
- A supported database (H2, MySQL, PostgreSQL, Oracle, OceanBase, etc.)

### 2. Add Dependency

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

### 3. Configuration

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

### 4. Define a Model

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

### 5. Enable Scanning

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

### 6. Use the Service

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
        return userService.queryChain()
                .where(c -> c.gt(User.Fields.age, 18))
                .exec();
    }
}
```

## Core Concepts

### Entity Modeling

Define your data models using standard Java classes with annotations.

#### 1. Annotations Reference

- **`@Model`**: Marks a class as a managed model.
  - `tableName`: Custom table name (default: derived from class name, e.g., `UserProfile` -> `user_profile`).
  - `comment`: Database table comment.
  - `logicDelete`: Enable logical deletion (requires a field like `Integer deleted`).
  - `disableTableCreateAndAlter`: Disable auto-DDL for this specific model.

- **`@IdField`**: Marks the primary key field.
  - `keyGeneratorMode`: ID generation strategy (e.g., `UUID`, `AUTO`, `SNOWFLAKE`).
  - `order`: **Required for Composite Primary Keys**. Specifies the key order (0, 1, ...).
  - `ddlColumnType`: Manually specify column type (e.g., `VARCHAR`). Note: Specify length in `ddlCharacterMaximumLength`.

- **`@BasicField`**: Maps a field to a standard database column.
  - `columnName`: Custom column name.
  - `ddlNotNull`: Column cannot be null (default: `false`).
  - `ddlDefaultValue`: Default value definition (e.g., `0`, `'active'`).
  - `ddlCharacterMaximumLength`: Max length for string columns.
  - `ddlNumericPrecision` / `ddlNumericScale`: Precision for numeric types.
  - `ddlIndex`: Create an index (default: `false`).
  - `ddlIndexType`: Type of index (`NORMAL`, `UNIQUE`).
  - `ddlComment`: Column comment.

- **`@ToOne`**: Defines a "Many-to-One" or "One-to-One" relationship.
  - `targetModel`: The name of the target model (optional if field type is a Model).
  - `joinField`: The foreign key field in this model (default: `{fieldName}Id`).

- **`@ToMany`**: Defines a "One-to-Many" or "Many-to-Many" relationship.
  - `targetModel`: The name of the target model (optional if List generic type is a Model).
  - `joinField`: The foreign key field in the target model (default: `{thisModelName}Id`).

- **`@IgnoreField`**: Excludes the field from database mapping.

#### 2. Simple Relationships

**One-to-One / Many-to-One:**

```java
@Data
@Model
public class User {
    @IdField
    private String id;
    
    // Auto-generates "departmentId" column in User table
    @ToOne 
    private Department department;
}
```

**One-to-Many:**

```java
@Data
@Model
public class Department {
    @IdField
    private String id;
    
    // Expects "departmentId" column in User table
    @ToMany 
    private List<User> users;
}
```

#### 3. Composite Primary Keys

Annotate multiple fields with `@IdField` and specify their `order`.

```java
@Model
public class UserRole {
    @IdField(order = 0)
    private String userId;

    @IdField(order = 1)
    private String roleId;
    
    // ...
}
```

#### 4. Many-to-Many Relationships

Implement Many-to-Many using an intermediate entity with a composite primary key.

**Entity A: Student**
```java
@Data
@Model
public class Student {
    @IdField
    private String id;
    private String name;

    @ToMany(targetModel = "StudentCourse")
    private List<StudentCourse> studentCourses;
}
```

**Entity B: Course**
```java
@Data
@Model
public class Course {
    @IdField
    private String id;
    private String title;
}
```

**Intermediate Entity: StudentCourse**
```java
@Data
@Model
public class StudentCourse {
    @IdField(order = 0)
    private String studentId;

    @IdField(order = 1)
    private String courseId;

    @ToOne(targetModel = "Course")
    private Course course;
}
```

#### 5. Inheritance (Single Table)

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

### Extensibility (`ExtBean`)

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

## Data Management

### 1. CRUD Operations

The `BaseService` (and underlying `DataManager`) provides standard CRUD methods.

```java
// Insert
User user = new User();
user.setName("Alice");
userService.insert(user);

// Update (updates non-null fields by default)
user.setName("Alice Updated");
userService.update(user);

// Get by ID
User found = userService.getById(user.getId());

// Delete
userService.delete(user.getId());
```

### 2. DataManager with Maps

The `DataManager` interface supports `Map<String, Object>` for scenarios without entity classes.

```java
// Get DataManager
DataManager<Integer> dataManager = modelService.getDataManager("User");

// Insert Map
Map<String, Object> data = new HashMap<>();
data.put("name", "Bob");
Integer id = dataManager.insert(data);

// Query returning Maps
List<Map<String, Object>> results = dataManager.queryChain()
        .where(c -> c.gt("age", 20))
        .exec();
```

### 3. Fluent Query API

Construct complex queries with automatic logical precedence (`AND` > `OR`).

#### Simple Query
```java
List<User> users = userService.queryChain()
        .where(c -> c.eq(User.Fields.name, "John"))
        .asc(User.Fields.age)
        .exec();
```

#### Complex Logic
```java
// WHERE (age > 18 AND status = 'Active') OR role = 'Admin'
userService.queryChain()
        .where(c -> c.bracket(b -> b.gt("age", 18).eq("status", "Active"))
                     .or(b -> b.eq("role", "Admin")))
        .exec();
```

#### Join Queries
Fetch related entities using `.joins()`.

```java
// Fetch User and their Department
List<User> users = userService.queryChain()
        .joins(Join.of("department"))
        .exec();

// Fetch Student and their Courses (via StudentCourse)
List<Student> students = studentService.queryChain()
        .joins(Join.of("studentCourses.course")) 
        .exec();
```

### 4. Recursive Queries

Retrieve hierarchical data (e.g., Department Tree).

```java
Map<String, Object> tree = departmentService.getRecursiveTreeById("dept-id");
```

## Advanced Features

### 1. Interceptors

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

### 2. Auto-Fillers

Automatically populate fields (e.g., `createTime`, `updateUser`). Implement `Filler` or extend `AbstractCreatorFiller`.

### 3. Multi-Tenancy



Isolate data using separate `ModelService` instances (different table prefixes) or Row-Level Permissions via `Permission` interface.



## Community



- **Issues**: Please file an issue for bugs or feature requests.

- **Discussions**: Join the discussions on GitHub.



## License



This project is licensed under the [Apache License 2.0](LICENSE).
