# API Reference

This document serves as the comprehensive reference for Annotations and the `ConditionBuilder` API.

## 1. Annotation Reference

This section details every annotation and attribute available for defining your data models. These annotations control both the runtime behavior (SQL generation) and the schema synchronization (DDL).

### 1.1 `@Model`
**Target**: Class
**Purpose**: Marks a class as a managed database entity.

| Attribute | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `tableName` | String | `""` | The database table name. If empty, it's derived from the class name (e.g., `User` -> `t_user` based on config). |
| `comment` | String | `""` | The comment applied to the database table (DDL). |
| `logicDelete` | boolean | `false` | If `true`, enables logical deletion. `delete()` operations will update the `del_flag` instead of removing the row. |
| `autoField` | boolean | `true` | If `true`, all fields in the class are automatically mapped to DB columns unless annotated with `@IgnoreField`. If `false`, you must explicitly annotate fields with `@BasicField`. |
| `disableTableCreateAndAlter` | boolean | `false` | If `true`, the framework will **never** execute DDL (CREATE/ALTER) for this table. Safe mode for legacy tables. |
| `partition` | `@Partition` | - | Database partitioning configuration (e.g., specific to MySQL/Oracle). |

### 1.2 `@IdField`
**Target**: Field
**Purpose**: Marks the Primary Key.

| Attribute | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `keyGeneratorMode` | Enum | `DEFAULT` | ID generation strategy. Options: `UUID`, `SNOWFLAKE`, `IDENTITY` (Auto-increment), `NONE`. |
| `order` | int | `0` | **Crucial for Composite Keys**. Defines the order of columns in the Primary Key definition. |
| `columnName` | String | `""` | Custom DB column name. |
| `ddlColumnType` | String | `""` | Manually specify SQL type (e.g., `VARCHAR(64)`). Overrides auto-inference. |
| `ddlComment` | String | `""` | Column comment. |

### 1.3 `@BasicField`
**Target**: Field
**Purpose**: Configuration for standard columns.

| Attribute | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `columnName` | String | `""` | Custom DB column name. |
| `select` | boolean | `true` | If `false`, this field is **excluded** from `SELECT *` queries by default (useful for huge `TEXT` or `BLOB` fields). |
| `typeHandler` | Class | `TypeHandler` | Custom MyBatis `TypeHandler` for runtime data conversion (e.g., JSON to Object). |
| `ddlNotNull` | boolean | `false` | Adds `NOT NULL` constraint in DDL. |
| `ddlDefaultValue` | String | `""` | Sets default value in DDL (e.g., `'0'`, `'ACTIVE'`). |
| `ddlIndex` | boolean | `false` | If `true`, creates an index for this column. |
| `ddlIndexType` | IndexType | `NORMAL` | Index type: `NORMAL`, `UNIQUE`. |
| `ddlCharacterMaximumLength` | int | - | Max length for `VARCHAR`. Defaults to 255 if unspecified. |
| `ddlNumericPrecision` | int | - | Total digits for `DECIMAL`. |
| `ddlNumericScale` | int | - | Decimal places for `DECIMAL`. |

### 1.4 `@ToOne`
**Target**: Field (Object)
**Purpose**: Defines Many-to-One or One-to-One relationships.

| Attribute | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `targetModel` | String | `""` | Name of the target model. If empty, inferred from the field's type. |
| `joinLocalFields` | String[] | `{}` | The foreign key column(s) in the **current** table. Default: `fieldName` + `Id`. |

### 1.5 `@ToMany`
**Target**: Field (List)
**Purpose**: Defines One-to-Many relationships.

| Attribute | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `targetModel` | String | `""` | Name of the target model. If empty, inferred from `List<GenericType>`. |
| `joinTargetFields` | String[] | `{}` | The foreign key column(s) in the **target** table. Default: `CurrentModelName` + `Id`. |

---

## 2. ConditionBuilder API Reference

The `ConditionBuilder` is the heart of the Fluent Query API. It supports Type-Safe Lambda expressions (`SFunction`) to prevent "Magic String" errors.

### 2.1 Equality & Simple Comparison

| Method | Lambda Equivalent | Description |
| :--- | :--- | :--- |
| `.eq("age", 18)` | `.eq(User::getAge, 18)` | Equal (`=`) |
| `.ne("age", 18)` | `.ne(User::getAge, 18)` | Not Equal (`!=` or `<>`) |
| `.gt("age", 18)` | `.gt(User::getAge, 18)` | Greater Than (`>`) |
| `.gte("age", 18)` | `.gte(User::getAge, 18)` | Greater Than or Equal (`>=`) |
| `.lt("age", 18)` | `.lt(User::getAge, 18)` | Less Than (`<`) |
| `.lte("age", 18)` | `.lte(User::getAge, 18)` | Less Than or Equal (`<=`) |

### 2.2 String Operations

| Method | Lambda Equivalent | SQL Translation |
| :--- | :--- | :--- |
| `.like("name", "A")` | `.like(User::getName, "A")` | `name LIKE 'A'` (Literal) |
| `.contains("name", "A")` | `.contains(...)` | `name LIKE '%A%'` |
| `.startsWith("name", "A")` | `.startsWith(...)` | `name LIKE 'A%'` |
| `.endsWith("name", "A")` | `.endsWith(...)` | `name LIKE '%A'` |

### 2.3 List & Null Checks

| Method | Lambda Equivalent | Description |
| :--- | :--- | :--- |
| `.in("status", list)` | `.in(User::getStatus, list)` | `IN ('A', 'B')` |
| `.notIn("status", list)` | `.notIn(...)` | `NOT IN (...)` |
| `.isNull("email")` | `.isNull(User::getEmail)` | `email IS NULL` |
| `.isNotNull("email")` | `.isNotNull(...)` | `email IS NOT NULL` |
| `.isBlank("name")` | `.isBlank(...)` | Checks for null or empty string `''` |

### 2.4 The "Optional" Variants (Dynamic Filtering)
Every method above (except `isNull`) has an `*Optional` variant (e.g., `eqOptional`, `likeOptional`, `inOptional`).

**Why?**
In web search forms, fields are often empty if the user didn't type anything.
- **`.eq("name", null)`**: Generates `name = null` (which matches nothing in SQL).
- **`.eqOptional("name", null)`**: **Ignores** the condition entirely. The SQL `WHERE` clause is not modified.

```java
// Common Pattern: Search Form
public List<User> search(String name, Integer age) {
    return userService.query(c -> c
        .likeOptional(User::getName, name) // If name is null/"", this line does nothing
        .gtOptional(User::getAge, age)     // If age is null, this line does nothing
    );
}
```

### 2.5 Logic & Grouping (Nested Conditions)

You can combine conditions using `.and()`, `.or()`, and `.bracket()`.

**Standard Chain (Implicit AND)**
```java
// WHERE age > 18 AND status = 1
c.gt("age", 18).eq("status", 1)
```

**OR Condition**
```java
// WHERE age > 18 OR status = 1
c.gt("age", 18).or().eq("status", 1)
```

**Nested Grouping (Brackets)**
```java
// WHERE (status = 1 AND age > 18) OR (role = 'ADMIN')
c.bracket(b -> b.eq("status", 1).gt("age", 18))
 .or(b -> b.eq("role", "ADMIN"))
```

### 2.6 Exists (Subqueries)

Supports `EXISTS` clauses for advanced filtering across related tables.

```java
// Find users who have placed orders > $100
// WHERE EXISTS (SELECT 1 FROM t_order WHERE user_id = t_user.id AND amount > 100)
c.exists("orders", sub -> sub.gt("amount", 100))
```
- `"orders"` refers to the `@ToMany` field name in the `User` class.

---

## 3. Lambda Support (Type-Safety)

Using "Magic Strings" for field names (e.g., `.eq("usrName", ...)` instead of `"userName"`) is a common source of bugs. `ConditionBuilder` supports Java 8 Method References (`SFunction`) to resolve column names from getters.

**Benefits**:
1.  **Compile-time Check**: If you rename the field in the Java class, the query code won't compile (instead of failing at runtime).
2.  **IDE Support**: Auto-completion works perfectly.

**Usage**:
Simply replace the String parameter with a method reference `Class::getField`.

```java
@Data
@Model
public class User {
    private String name;
    private Integer age;
}

// Type-Safe Query
userService.query(c -> c
    .eq(User::getName, "Alice")
    .gt(User::getAge, 18)
);
```

**Note**: This requires your entity class to have standard Getter methods. Lombok `@Data` works perfectly.
