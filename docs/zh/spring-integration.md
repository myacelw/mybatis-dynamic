# Spring 集成

`spring` 模块将 MyBatis Dynamic 无缝集成到 Spring 生态系统中，提供自动配置、依赖注入和 REST 功能。

## 自动配置

`DynamicModelAutoConfiguration` 类处理设置。当 `mybatis-dynamic-spring` 在类路径上时，它会自动触发。

### 配置属性

使用 `mybatis-dynamic` 前缀在 `application.yml`（或 `properties`）中配置框架。

| 属性 | 类型 | 默认值 | 描述 |
|----------|------|---------|-------------|
| `mybatis-dynamic.update-model` | `boolean` | `true` | 为 true 时，在启动时自动将数据库模式 (DDL) 与 Java 模型同步。 |
| `mybatis-dynamic.dialect` | `String` | `null` | 显式设置数据库方言（例如 `mysql`、`h2`、`postgresql`）。如果为 null，则尝试自动检测。 |
| `mybatis-dynamic.table-prefix` | `String` | `""` | 附加到所有自动生成表名的前缀。 |
| `mybatis-dynamic.index-prefix` | `String` | `idx_` | 自动生成索引的前缀。 |
| `mybatis-dynamic.seq-prefix` | `String` | `seq_` | 序列的前缀 (Oracle/Postgres)。 |
| `mybatis-dynamic.rest.enabled` | `boolean` | `true` | 启用 `DynamicModelController` 以暴露通用 REST 端点。 |
| `mybatis-dynamic.ddl.dry-run` | `boolean` | `false` | 如果为 true，DDL 语句将被记录但不会执行。用于生产环境的安全检查。 |
| `mybatis-dynamic.ddl.log-path` | `String` | `null` | 执行的 DDL 语句将追加到的文件路径。 |

## 模型扫描与注入

要使用动态模型，必须启用扫描。

### 1. 启用扫描

将 `@EnableModelScan` 添加到你的 `@Configuration` 或主应用程序类中。

```java
@SpringBootApplication
@EnableModelScan(basePackages = "com.example.project.entity")
public class App { ... }
```

### 2. 依赖注入

对于每个扫描到的 `@Model` 类，框架都会注册一个 `DataManager` bean（特别是 `BaseService` 或 `BaseDao`）。你可以直接将这些注入到你的组件中。

**按泛型类型注入：**

```java
@Service
public class UserService {

    // 注入 User 实体的服务
    // 泛型类型 <Integer, User> 必须匹配你的实体定义
    @Autowired
    private BaseService<Integer, User> userService;

    public void register(User user) {
        userService.insert(user);
    }
}
```

**按名称注入：**
bean 名称遵循模式：`lowerCamelCase(ModelName) + Service`（例如，`User` -> `userService`）。

### 3. 服务定制

如果你需要超出标准 CRUD 的自定义业务逻辑，可以扩展 `BaseServiceImpl`。

```java
@Service
// 框架会检测到这一点，并使用你的 bean 而不是默认 bean
public class MyUserService extends BaseServiceImpl<Integer, User> {
    
    public void specializedBusinessMethod() {
        // ...
    }
}
```

## 动态 REST 控制器

`DynamicModelController` 为快速原型设计或管理面板提供零代码 API。

### 基础 URL
`/api/dynamic/{modelName}`

### 端点详情

| 方法 | URL 模式 | 功能 | 参数 |
|--------|-------------|----------|------------|
| **GET** | `/{model}` | 列表/搜索 | `page` (int), `size` (int), `sort` (field,asc), 加上任何字段过滤 (例如 `?name=Alice`) |
| **GET** | `/{model}/{id}` | 获取单个 | `id`: 路径变量 |
| **POST** | `/{model}` | 创建 | Body: JSON 对象 |
| **PUT** | `/{model}` | 更新 | Body: JSON 对象 (必须包含 ID) |
| **DELETE** | `/{model}/{id}` | 删除 | `id`: 路径变量 |

### 安全集成

REST 控制器遵循 `PermissionGetter` 逻辑。要保护这些端点：
1.  实现 `CurrentUserHolder`（参见 [高级功能](advanced-features.md)）。
2.  将其注册为 Spring Bean。

控制器将在每次请求之前自动调用 `getCurrentUserPermission()` 以过滤数据或阻止访问。
