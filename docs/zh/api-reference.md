# API 参考

本文档作为注解和 `ConditionBuilder` API 的综合参考。

## 1. 注解参考

本节详细介绍了用于定义数据模型的每个注解和属性。这些注解控制运行时行为（SQL 生成）和模式同步（DDL）。

### 1.1 `@Model`
**目标**：类
**用途**：将类标记为受管数据库实体。

| 属性 | 类型 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- |
| `tableName` | String | `""` | 数据库表名。如果为空，则从类名派生（例如，基于配置 `User` -> `t_user`）。 |
| `comment` | String | `""` | 应用于数据库表的注释 (DDL)。 |
| `logicDelete` | boolean | `false` | 如果为 `true`，则启用逻辑删除。`delete()` 操作将更新 `del_flag` 而不是删除行。 |
| `autoField` | boolean | `true` | 如果为 `true`，除非使用 `@IgnoreField` 注解，否则类中的所有字段都会自动映射到 DB 列。如果为 `false`，则必须使用 `@BasicField` 显式注解字段。 |
| `disableTableCreateAndAlter` | boolean | `false` | 如果为 `true`，框架将 **永远不会** 对此表执行 DDL (CREATE/ALTER)。用于遗留表的安全模式。 |
| `partition` | `@Partition` | - | 数据库分区配置（例如，特定于 MySQL/Oracle）。 |

### 1.2 `@IdField`
**目标**：字段
**用途**：标记主键。

| 属性 | 类型 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- |
| `keyGeneratorMode` | Enum | `DEFAULT` | ID 生成策略。选项：`UUID`、`SNOWFLAKE`、`IDENTITY`（自增）、`NONE`。 |
| `order` | int | `0` | **对于复合主键至关重要**。定义主键定义中列的顺序。 |
| `columnName` | String | `""` | 自定义数据库列名。 |
| `ddlColumnType` | String | `""` | 手动指定 SQL 类型（例如 `VARCHAR(64)`）。覆盖自动推断。 |
| `ddlComment` | String | `""` | 列注释。 |

### 1.3 `@BasicField`
**目标**：字段
**用途**：标准列的配置。

| 属性 | 类型 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- |
| `columnName` | String | `""` | 自定义数据库列名。 |
| `select` | boolean | `true` | 如果为 `false`，默认情况下该字段将从 `SELECT *` 查询中 **排除**（对于巨大的 `TEXT` 或 `BLOB` 字段很有用）。 |
| `typeHandler` | Class | `TypeHandler` | 用于运行时数据转换的自定义 MyBatis `TypeHandler`（例如，JSON 到对象）。 |
| `ddlNotNull` | boolean | `false` | 在 DDL 中添加 `NOT NULL` 约束。 |
| `ddlDefaultValue` | String | `""` | 在 DDL 中设置默认值（例如 `'0'`, `'ACTIVE'`）。 |
| `ddlIndex` | boolean | `false` | 如果为 `true`，则为此列创建索引。 |
| `ddlIndexType` | IndexType | `NORMAL` | 索引类型：`NORMAL`, `UNIQUE`。 |
| `ddlCharacterMaximumLength` | int | - | `VARCHAR` 的最大长度。如果未指定，默认为 255。 |
| `ddlNumericPrecision` | int | - | `DECIMAL` 的总位数。 |
| `ddlNumericScale` | int | - | `DECIMAL` 的小数位数。 |

### 1.4 `@ToOne`
**目标**：字段（对象）
**用途**：定义多对一或一对一关系。

| 属性 | 类型 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- |
| `targetModel` | String | `""` | 目标模型的名称。如果为空，则从字段类型推断。 |
| `joinLocalFields` | String[] | `{}` | **当前** 表中的外键列。默认值：`fieldName` + `Id`。 |

### 1.5 `@ToMany`
**目标**：字段（列表）
**用途**：定义一对多关系。

| 属性 | 类型 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- |
| `targetModel` | String | `""` | 目标模型的名称。如果为空，则从 `List<GenericType>` 推断。 |
| `joinTargetFields` | String[] | `{}` | **目标** 表中的外键列。默认值：`CurrentModelName` + `Id`。 |

---

## 2. ConditionBuilder API 参考

`ConditionBuilder` 是 Fluent Query API 的核心。它支持类型安全的 Lambda 表达式 (`SFunction`) 以防止“魔法字符串”错误。

### 2.1 相等性与简单比较

| 方法 | Lambda 等效项 | 描述 |
| :--- | :--- | :--- |
| `.eq("age", 18)` | `.eq(User::getAge, 18)` | 等于 (`=`) |
| `.ne("age", 18)` | `.ne(User::getAge, 18)` | 不等于 (`!=` 或 `<>`) |
| `.gt("age", 18)` | `.gt(User::getAge, 18)` | 大于 (`>`) |
| `.gte("age", 18)` | `.gte(User::getAge, 18)` | 大于等于 (`>=`) |
| `.lt("age", 18)` | `.lt(User::getAge, 18)` | 小于 (`<`) |
| `.lte("age", 18)` | `.lte(User::getAge, 18)` | 小于等于 (`<=`) |

### 2.2 字符串操作

| 方法 | Lambda 等效项 | SQL 转换 |
| :--- | :--- | :--- |
| `.like("name", "A")` | `.like(User::getName, "A")` | `name LIKE 'A'` (字面量) |
| `.contains("name", "A")` | `.contains(...)` | `name LIKE '%A%'` |
| `.startsWith("name", "A")` | `.startsWith(...)` | `name LIKE 'A%'` |
| `.endsWith("name", "A")` | `.endsWith(...)` | `name LIKE '%A'` |

### 2.3 列表与空值检查

| 方法 | Lambda 等效项 | 描述 |
| :--- | :--- | :--- |
| `.in("status", list)` | `.in(User::getStatus, list)` | `IN ('A', 'B')` |
| `.notIn("status", list)` | `.notIn(...)` | `NOT IN (...)` |
| `.isNull("email")` | `.isNull(User::getEmail)` | `email IS NULL` |
| `.isNotNull("email")` | `.isNotNull(...)` | `email IS NOT NULL` |
| `.isBlank("name")` | `.isBlank(...)` | 检查 null 或空字符串 `''` |

### 2.4 "Optional" 变体（动态过滤）
上面的每个方法（除了 `isNull`）都有一个 `*Optional` 变体（例如 `eqOptional`, `likeOptional`, `inOptional`）。

**为什么？**
在 Web 搜索表单中，如果用户没有输入任何内容，字段通常为空。
- **`.eq("name", null)`**: 生成 `name = null`（在 SQL 中不匹配任何内容）。
- **`.eqOptional("name", null)`**: 完全 **忽略** 该条件。SQL `WHERE` 子句未被修改。

```java
// 常见模式：搜索表单
public List<User> search(String name, Integer age) {
    return userService.query(c -> c
        .likeOptional(User::getName, name) // 如果 name 为 null/""，此行不执行任何操作
        .gtOptional(User::getAge, age)     // 如果 age 为 null，此行不执行任何操作
    );
}
```

### 2.5 逻辑与分组（嵌套条件）

你可以使用 `.and()`, `.or()`, 和 `.bracket()` 组合条件。

**标准链式（隐式 AND）**
```java
// WHERE age > 18 AND status = 1
c.gt("age", 18).eq("status", 1)
```

**OR 条件**
```java
// WHERE age > 18 OR status = 1
c.gt("age", 18).or().eq("status", 1)
```

**嵌套分组（括号）**
```java
// WHERE (status = 1 AND age > 18) OR (role = 'ADMIN')
c.bracket(b -> b.eq("status", 1).gt("age", 18))
 .or(b -> b.eq("role", "ADMIN"))
```

### 2.6 Exists (子查询)

支持 `EXISTS` 子句，用于跨相关表进行高级过滤。

```java
// 查找下过订单金额 > $100 的用户
// WHERE EXISTS (SELECT 1 FROM t_order WHERE user_id = t_user.id AND amount > 100)
c.exists("orders", sub -> sub.gt("amount", 100))
```
- `"orders"` 指的是 `User` 类中的 `@ToMany` 字段名称。

---

## 3. Lambda 支持（类型安全）

使用“魔法字符串”作为字段名称（例如 `.eq("usrName", ...)` 而不是 `"userName"`）是错误的常见来源。`ConditionBuilder` 支持 Java 8 方法引用 (`SFunction`) 以从 getter 解析列名。

**好处**：
1.  **编译时检查**：如果你重命名 Java 类中的字段，查询代码将无法编译（而不是在运行时失败）。
2.  **IDE 支持**：自动完成功能完美运行。

**用法**：
只需将 String 参数替换为方法引用 `Class::getField`。

```java
@Data
@Model
public class User {
    private String name;
    private Integer age;
}

// 类型安全查询
userService.query(c -> c
    .eq(User::getName, "Alice")
    .gt(User::getAge, 18)
);
```

**注意**：这要求你的实体类具有标准的 Getter 方法。Lombok `@Data` 完美适用。
