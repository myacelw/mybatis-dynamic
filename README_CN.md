# mybatis-dynamic

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.myacelw/mybatis-dynamic-core.svg)](https://search.maven.org/search?q=g:io.github.myacelw%20AND%20a:mybatis-dynamic-core)

**mybatis-dynamic** 是一个基于 MyBatis 构建的强大动态 ORM 框架。它允许开发者直接在 Java 代码中定义数据模型，自动管理数据库架构（表、列、索引），并提供丰富流畅的 API 用于 CRUD 操作和复杂查询。

## 主要特性

- **动态建模**: 将数据模型定义为 Java 类。框架会在启动或运行时自动生成和更新数据库架构 (DDL)。
- **运行时模型修改**: 支持在运行时通过编程方式修改模型，适应动态变化的业务需求。
- **丰富流畅的 API**: 使用自然、易读的语法构建复杂查询。支持自动逻辑优先级 (`AND` > `OR`)、嵌套分组和连表查询。
- **Spring Boot 集成**: 与 Spring Boot 无缝集成。只需注解您的模型，框架即可自动配置 `BaseDao` 和 `BaseService` bean。
- **高级映射**: 支持 `@ToOne`、`@ToMany` 和递归关系。
- **可视化**: 内置工具用于可视化模型关系。

## 架构

项目采用模块化设计，分离关注点：

- **`core`**: 核心引擎。处理动态建模、SQL 生成和查询执行。可独立使用。
- **`spring`**: Spring Boot 启动器。提供自动配置和便捷集成。
- **`draw`**: 可视化模块。提供 Web UI 以查看实体关系。
- **`sample`**: 一个完整的 Spring Boot 示例应用程序，演示了用法。

## 快速开始

### 1. 环境要求

- Java 8+
- Maven 或 Gradle
- 支持的数据库 (H2, MySQL, PostgreSQL, Oracle, OceanBase 等)

### 2. 添加依赖

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

### 3. 配置

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

### 4. 定义模型

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

### 5. 启用扫描

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

### 6. 使用 Service

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

## 文档

有关详细信息，请参阅以下部分：

- [实体建模与扩展](#实体建模与扩展)
- [流畅查询 API](#流畅查询-api)
- [高级功能](#高级功能)
