# Say Goodbye to XML: Dynamic Data Modeling with mybatis-dynamic

> Author: Liu Wei

---

## Introduction

As a Java developer, you've likely faced these scenarios:

- Product manager says "add a field", and you have to modify the Entity, Mapper XML, write migration scripts, redeploy...
- You want to quickly prototype an idea but spend half a day configuring ORM.
- Building a low-code platform where users need dynamic table creation, and traditional ORMs just give up.

MyBatis is flexible but XML configuration is tedious; JPA is automated but not flexible enough. Is there a solution that offers the convenience of "Code as Model" while retaining MyBatis's flexibility?

**mybatis-dynamic** is that attempt—a dynamic ORM framework built on MyBatis that lets you define data models in Java code, automatically handling table creation, modification, querying, and even exposing REST APIs directly.

---

## 1. What is mybatis-dynamic?

The core philosophy is **"Model as Truth"**:

- You define data models using Java classes.
- The framework automatically generates and maintains database table structures.
- Models can be modified at runtime.
- Built-in Fluent API, saying goodbye to XML and string concatenation.

It doesn't replace MyBatis but adds a dynamic layer on top. Ideal for rapid prototyping, dynamic business logic, and low-code platforms.

---

## 2. Quick Start in 5 Minutes

### 2.1 Add Dependencies

```xml
<dependency>
    <groupId>io.github.myacelw</groupId>
    <artifactId>mybatis-dynamic-spring</artifactId>
    <version>latest-version</version>
</dependency>
<!-- Database driver, e.g., H2 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2.2 Configuration

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:./test;MODE=MySQL
    username: sa
    password:

mybatis-dynamic:
  update-model: true   # Auto-sync table structure on startup
  table-prefix: t_     # Table prefix
```

### 2.3 Define Model

```java
@Data
@FieldNameConstants
@Model(comment = "User Table")
public class User {

    @IdField
    private Integer id;

    @BasicField(ddlComment = "Username", ddlNotNull = true)
    private String name;

    @BasicField(ddlComment = "Age")
    private Integer age;
}
```

### 2.4 Enable Scanning

```java
@SpringBootApplication
@EnableModelScan(basePackages = "com.example.demo.model")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2.5 Use It

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private BaseService<Integer, User> userService;

    @PostMapping
    public Integer create(@RequestBody User user) {
        return userService.insert(user);
    }

    @GetMapping
    public List<User> list() {
        return userService.query()
                .where(c -> c.gt(User.Fields.age, 18))
                .exec();
    }
}
```

**Noticed anything?**

- No Mapper XML
- No manual DDL
- No `@Repository`
- `BaseService` auto-injected

Start the app, tables are created, APIs are ready.

---

## 3. Core Features

### 3.1 Dynamic DDL: Model Changes, Table Follows

This is the core capability. With `update-model: true`:

- **Add Field**: Add a property in Java class, restart, column added.
- **Add Index**: Add `ddlIndex = true`, restart, index created.
- **Change Comment**: Modify `ddlComment`, restart, comment updated.

### 3.2 Fluent Query API

No more SQL string concatenation nightmares:

```java
userService.query()
    .select("id", "name", "department.name")  // Auto JOIN
    .where(c -> c.eq("status", 1)
                 .and(sub -> sub.gt("age", 18)
                                .or()
                                .eq("vip", true)))
    .orderByDesc("createdAt")
    .limit(10)
    .exec();
```

### 3.3 Relations: @ToOne / @ToMany

Define relationships in your model and let the framework handle joins automatically.

### 3.4 Zero-Code REST API

Instant CRUD endpoints (`GET`, `POST`, `PUT`, `DELETE`) at `/api/dynamic/{model}`.

### 3.5 Runtime Dynamic Fields

Implement `ExtBean` to support arbitrary runtime fields (e.g., for SaaS custom fields).

### 3.6 Visualization (Draw)

Built-in ER diagram generation at `/draw/index.html`.

### 3.7 Enterprise Permission Control

Row-level and Column-level security via `PermissionGetter` interface.

---

## 4. Scenarios

### ✅ Recommended
- Rapid Prototyping
- Internal Management Systems
- Low-Code / Dynamic Form Platforms
- SaaS with Custom Fields

### ⚠️ Trade-offs
- Ultra-high concurrency core systems (runtime overhead)
- Migrating legacy MyBatis projects
- Complex reporting (Raw SQL might be better)

---

## Conclusion

mybatis-dynamic solves a specific pain point: **When you need data models to be dynamic, traditional ORMs become a bottleneck.**

Check it out on GitHub: [https://github.com/myacelw/mybatis-dynamic](https://github.com/myacelw/mybatis-dynamic)
