# 快速入门

本指南将帮助你使用 MyBatis Dynamic 搭建一个简单的 Spring Boot 应用程序。

## 先决条件

*   Java 8+
*   Maven
*   Spring Boot 2.x 或 3.x

## 1. 添加依赖

将 `mybatis-dynamic-spring` 依赖添加到你的 `pom.xml` 文件中。

```xml
<dependency>
    <groupId>io.github.myacelw</groupId>
    <artifactId>mybatis-dynamic-spring</artifactId>
    <version>${mybatis-dynamic.version}</version>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <runtime>true</runtime>
</dependency>
```

## 2. 配置应用程序

在 `application.yml` 中配置数据库和 MyBatis Dynamic 设置。

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver

mybatis-dynamic:
  # 启用自动建表/更新表
  update-model: true
  # 数据库对象前缀
  table-prefix: "t_"
  index-prefix: "idx_"
  seq-prefix: "seq_"
  # 启用内置 REST API
  rest:
    enabled: true
  dialect: h2
```

## 3. 创建模型

使用 `@Model` 注解定义你的数据模型。

```java
package com.example.demo.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.model.BaseEntity;
import lombok.Data;

@Data
@Model(comment = "用户账号")
public class User extends BaseEntity<Integer> {

    @BasicField(comment = "用户名")
    private String username;

    @BasicField(comment = "邮箱地址")
    private String email;
}
```

## 4. 启用模型扫描

在主应用程序类上添加 `@EnableModelScan` 以自动发现模型。

```java
package com.example.demo;

import io.github.myacelw.mybatis.dynamic.spring.registrar.EnableModelScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableModelScan(basePackages = "com.example.demo.entity")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 5. 运行并验证

启动你的应用程序。框架将：
1.  扫描 `@Model` 类。
2.  在 H2 数据库中自动创建 `t_user` 表。
3.  暴露 REST 端点（如果已启用）。

你可以访问动态 REST API：
`GET /api/dynamic/User`
