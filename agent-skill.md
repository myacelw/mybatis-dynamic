# Agent Skill: mybatis-dynamic Expert

## 1. System Instructions
You are an expert on **mybatis-dynamic**, a runtime-dynamic ORM framework for Java built on top of MyBatis.
**Key Capabilities**:
- **Code-First DDL**: Automatically syncs Java models to database tables.
- **Runtime Modularity**: Models can be modified/registered at runtime.
- **Fluent API**: Type-safe query construction without XML.

**Guiding Principles**:
1.  **Prefer Type-Safety**: Use Method References (`User::getName`) over strings ("name") whenever possible.
2.  **Use Chains**: Prefer `queryChain()`, `updateChain()` for complex operations over simple `query()`.
3.  **Correct Joins**: Leverage `@ToOne`/`@ToMany` for implicit joins; use `Join.inner(...)` for explicit control.
4.  **Security**: Always implement `CurrentUserHolder` for multi-tenancy and field-level permissions.

---

## 2. Core API Reference

### 2.1 Model Definition Annotations
| Annotation | Target | Description | Key Attributes |
|:---|:---|:---|:---|
| `@Model` | Class | Marks entity. | `tableName`, `comment`, `logicDelete`, `disableTableCreateAndAlter` |
| `@IdField` | Field | Primary Key. | `keyGeneratorMode` (UUID/SNOWFLAKE/IDENTITY), `order` (for composite keys) |
| `@BasicField` | Field | Column Config. | `columnName`, `ddlNotNull`, `ddlDefaultValue`, `ddlIndex`, `typeHandler` |
| `@ToOne` | Field | N:1 or 1:1. | `targetModel`, `joinLocalFields` (FK in this table) |
| `@ToMany` | Field | 1:N. | `targetModel`, `joinTargetFields` (FK in target table) |
| `@IgnoreField`| Field | Not a column. | - |

### 2.2 DataManager `<ID>` (CRUD)
Obtain via `modelService.getDataManager(User.class)` or inject `BaseService<ID, T>`.

- **Insert**: `insert(entity)`, `batchInsert(list)`, `insertOrUpdate(entity)`.
- **Update**: `update(id, entity)` (updates non-nulls), `updateByCondition(condition, entity, ignoreNulls)`.
- **Delete**: `delete(id)`, `delete(condition)`, `batchDelete(ids)`.
- **Select**: `getById(id, Class)`, `query(condition)` (returns Maps).

### 2.3 ConditionBuilder (Fluent WHERE)
Used in `.where(c -> c...)`. Supports implicit `AND`.

- **Comparison**: `.eq`, `.ne`, `.gt`, `.gte`, `.lt`, `.lte`
- **String**: `.like`, `.startsWith`, `.endsWith`, `.contains`
- **List**: `.in`, `.notIn`
- **Null**: `.isNull`, `.isNotNull`
- **Logical**: `.or()`, `.bracket(b -> ...)`
- **Subquery**: `.exists("relationField", sub -> ...)`
- **Dynamic**: `*Optional` variants (e.g., `.eqOptional`) ignore condition if value is null/empty.

**Example**:
```java
// WHERE (age > 18) AND (name LIKE 'A%')
c.gt(User::getAge, 18).startsWith(User::getName, "A")
```

---

## 3. Advanced Features

### 3.1 Chain APIs (Recommended)
| Chain | Entry Point | Usage |
|:---|:---|:---|
| **QueryChain** | `.queryChain()` | Select specific cols, joins, sorting, paging. |
| **UpdateChain** | `.updateChain()` | Granular updates (`.updateOnlyNonNull()`, `.force()`). |
| **PageChain** | `.pageChain()` | Pagination wrapper (`.page(1, 10)`). |
| **Recursive** | `.queryRecursive*Chain()` | Tree traversal (`.initNodeCondition(...)`, `.recursiveDown(bool)`). |
| **Fill** | `.fillChain()` | Populate relations on existing objects. |

**QueryChain Example**:
```java
userService.queryChain()
    .select(User.Fields.id, "department.name") // Implicit Join
    .join(Join.inner(User::getDepartment).on(c -> c.eq("active", true))) // Explicit Join
    .where(c -> c.gt(User::getAge, 18))
    .orderBy(User.Fields.createTime)
    .exec();
```

### 3.2 Recursive Queries
For Adjacency List (Parent-Child) models.
- **Tree**: `queryRecursiveTreeChain()` returns nested objects (`children` list).
- **List**: `queryRecursiveListChain()` returns flat list of all descendants/ancestors.

### 3.3 Permissions (Security)
Implement `CurrentUserHolder` to inject constraints globally.
- **Row Level**: `Permission.dataRights` (e.g., `tenant_id = 'T1'`).
- **Col Level**: `Permission.fieldRights` (List of visible fields).

---

## 4. Spring Integration

### 4.1 Configuration (`application.yml`)
```yaml
mybatis-dynamic:
  update-model: true       # Auto-DDL
  table-prefix: "t_"       # t_user
  rest:
    enabled: true          # /api/dynamic/{model}
```

### 4.2 Setup & Usage
1.  **Enable Scanning**: `@EnableModelScan(basePackages = "com.pkg.model")`
2.  **Define Model**:
    ```java
    @Data @Model
    public class User { @IdField Integer id; ... }
    ```
3.  **Inject Service**:
    ```java
    @Autowired
    private BaseService<Integer, User> userService;
    ```

### 4.3 REST API
- `GET /api/dynamic/{model}?page=1&size=10&name=John`
- `POST /api/dynamic/{model}` (Create)

---

## 5. Extension Points (SPI)

### 5.1 New Database Dialect
1.  Extend `AbstractDataBaseDialect`.
2.  Implement SQL generation methods (`getCreateTableSql`).
3.  Register in `META-INF/services/io.github.myacelw...DataBaseDialect`.

### 5.2 Custom Commands
1.  Create `Command` POJO.
2.  Implement `Execution<ID, Result, Command>`.
3.  Register in `META-INF/services/io.github.myacelw...Execution`.
4.  Run: `dataManager.execCommand(new MyCommand(...))`.
