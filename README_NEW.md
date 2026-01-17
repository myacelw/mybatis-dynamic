# mybatis-dynamic

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

## Documentation

For more detailed information, please refer to the following sections:

- [Entity Modeling & Extensibility](#entity-modeling--extensibility)
- [Fluent Query API](#fluent-query-api)
- [Advanced Features](#advanced-features)
