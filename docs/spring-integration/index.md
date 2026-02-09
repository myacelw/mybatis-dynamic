# Spring Integration

The `spring` module seamlessly integrates MyBatis Dynamic into the Spring ecosystem, providing auto-configuration, dependency injection, and REST capabilities.

## Auto Configuration

The `DynamicModelAutoConfiguration` class handles the setup. It triggers automatically when `mybatis-dynamic-spring` is on the classpath.

### Configuration Properties

Configure the framework in `application.yml` (or `properties`) using the `mybatis-dynamic` prefix.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `mybatis-dynamic.update-model` | `boolean` | `true` | When true, automatically synchronizes the database schema (DDL) with your Java models on startup. |
| `mybatis-dynamic.dialect` | `String` | `null` | Explicitly set the database dialect (e.g., `mysql`, `h2`, `postgresql`). If null, it attempts to auto-detect. |
| `mybatis-dynamic.table-prefix` | `String` | `""` | A string prepended to all auto-generated table names. |
| `mybatis-dynamic.index-prefix` | `String` | `idx_` | Prefix for auto-generated indexes. |
| `mybatis-dynamic.seq-prefix` | `String` | `seq_` | Prefix for sequences (Oracle/Postgres). |
| `mybatis-dynamic.rest.enabled` | `boolean` | `true` | Enables the `DynamicModelController` to expose generic REST endpoints. |
| `mybatis-dynamic.ddl.dry-run` | `boolean` | `false` | If true, DDL statements are logged but not executed. Useful for production safety checks. |
| `mybatis-dynamic.ddl.log-path` | `String` | `null` | Path to a file where executed DDL statements will be appended. |

## Model Scanning & Injection

To use dynamic models, you must enable scanning.

### 1. Enable Scanning

Add `@EnableModelScan` to your `@Configuration` or main application class.

```java
@SpringBootApplication
@EnableModelScan(basePackages = "com.example.project.entity")
public class App { ... }
```

### 2. Dependency Injection

For every scanned `@Model` class, the framework registers a `DataManager` bean (specifically a `BaseService` or `BaseDao`). You can inject these directly into your components.

**Injection by Generic Type:**

```java
@Service
public class UserService {

    // Inject service for User entity
    // The generic types <Integer, User> must match your entity definition
    @Autowired
    private BaseService<Integer, User> userService;

    public void register(User user) {
        userService.insert(user);
    }
}
```

**Injection by Name:**
The bean name follows the pattern: `lowerCamelCase(ModelName) + Service` (e.g., `User` -> `userService`).

### 3. Service Customization

If you need custom business logic beyond standard CRUD, you can extend `BaseServiceImpl`.

```java
@Service
// The framework detects this and uses YOUR bean instead of the default one
public class MyUserService extends BaseServiceImpl<Integer, User> {
    
    public void specializedBusinessMethod() {
        // ...
    }
}
```

## Dynamic REST Controller

The `DynamicModelController` provides a zero-code API for rapid prototyping or admin panels.

### Base URL
`/api/dynamic/{modelName}`

### Endpoints Detail

| Method | URL Pattern | Function | Parameters |
|--------|-------------|----------|------------|
| **GET** | `/{model}` | List/Search | `page` (int), `size` (int), `sort` (field,asc), plus any field filters (e.g., `?name=Alice`) |
| **GET** | `/{model}/{id}` | Get One | `id`: Path variable |
| **POST** | `/{model}` | Create | Body: JSON object |
| **PUT** | `/{model}` | Update | Body: JSON object (must include ID) |
| **DELETE** | `/{model}/{id}` | Delete | `id`: Path variable |

### Security Integration

The REST controller respects the `PermissionGetter` logic. To secure these endpoints:
1.  Implement `CurrentUserHolder` (see [Advanced Features](advanced-features.md)).
2.  Register it as a Spring Bean.

The controller will automatically invoke `getCurrentUserPermission()` before every request to filter data or block access.
