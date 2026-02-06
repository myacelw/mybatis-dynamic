# Quick Start

This guide will help you set up a simple Spring Boot application using MyBatis Dynamic.

## Prerequisites

*   Java 8+
*   Maven
*   Spring Boot 2.x or 3.x

## 1. Add Dependencies

Add the `mybatis-dynamic-spring` dependency to your `pom.xml`.

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

## 2. Configure Application

Configure your database and MyBatis Dynamic settings in `application.yml`.

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver

mybatis-dynamic:
  # Enable automatic table creation/update
  update-model: true
  # Prefixes for database objects
  table-prefix: "t_"
  index-prefix: "idx_"
  seq-prefix: "seq_"
  # Enable built-in REST API
  rest:
    enabled: true
  dialect: h2
```

## 3. Create a Model

Define your data model using the `@Model` annotation.

```java
package com.example.demo.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.model.BaseEntity;
import lombok.Data;

@Data
@Model(comment = "User Account")
public class User extends BaseEntity<Integer> {

    @BasicField(comment = "User Name")
    private String username;

    @BasicField(comment = "Email Address")
    private String email;
}
```

## 4. Enable Model Scanning

Add `@EnableModelScan` to your main application class to automatically discover models.

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

## 5. Run and Verify

Start your application. The framework will:
1.  Scan for `@Model` classes.
2.  Automatically create the `t_user` table in the H2 database.
3.  Expose REST endpoints (if enabled).

You can access the dynamic REST API at:
`GET /api/dynamic/User`
