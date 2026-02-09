# 2. 连接操作 (Join Operations)

有效的数据检索通常涉及查询多个相关表。`mybatis-dynamic` 支持隐式（自动）和显式（手动）连接策略。

### 2.1 概念: @ToOne 和 @ToMany

框架依赖模型注解来理解关系：

- **`@ToOne`**: 表示 `一对一` 或 `多对一` 关系。
  - *示例*: 一个 `User` 属于一个 `Department`。
- **`@ToMany`**: 表示 `一对多` 关系。
  - *示例*: 一个 `Department` 有许多 `User`。

这些注解告诉框架 *如何* 连接表（哪些列充当外键），而无需您每次都手动编写 `ON` 子句。

### 2.2 隐式连接 (自动)

最简单的连接方式就是直接请求数据。如果您选择 `@ToOne` 相关实体的字段，框架会自动生成一个 **Left Join**。

```java
// "department" 是 User 中的 @ToOne 字段
userService.queryChain()
    .select(User.Fields.name, "department.name") // 选择 user.name 和 department.name
    .where(c -> c.eq("department.status", "ACTIVE")) // 在连接表上添加过滤
    .exec();
```
* **机制**: 框架看到 `department.` 路径，从 `User` 模型解析关系，并追加 `LEFT JOIN department ON user.department_id = department.id`。

### 2.3 显式连接 (Explicit Joins)

为了精确控制连接类型或条件，请使用 `QueryChain` 中的 `.join()` 方法。

#### 连接类型
```java
userService.queryChain()
    // 标准 Inner Join
    .join(Join.inner("department"))
    
    // Left Join (如果未指定类型，则为默认值)
    .join(Join.left("department"))
    
    // Right Join
    .join(Join.right("department"))
    .exec();
```

#### 自定义 ON 条件
您可以向 `ON` 子句添加额外约束，这对于外连接来说通常比将它们放在 `WHERE` 子句中更有效。

```java
.join(Join.inner("department")
          .on(c -> c.eq("status", "ACTIVE")
                    .gt("createTime", someDate)))
```

### 2.4 嵌套和链式连接

您可以使用点符号跨多个层级的关系进行连接。

```java
// User -> Department -> Company
userService.queryChain()
    .join(Join.of("department.company")) 
    .select(User.Fields.name, "department.company.name")
    .exec();
```
* 这会自动先连接 `Department`，然后将 `Company` 连接到 `Department`。

### 2.5 动态连接 (一对多)

虽然 `@ToOne` 连接常用于获取单个相关记录，但您也可以连接 `@ToMany` 集合。

```java
// Department -> Users
departmentService.queryChain()
    .join(Join.of("users")) // 连接 User 表
    .where(c -> c.eq("users.status", "BANNED")) // 查找拥有被封禁用户的部门
    .exec();
```
* **注意**: 这可能会导致 SQL 结果集中出现“重复”的 Department 行（笛卡尔积），框架在映射回对象时会处理这个问题。

### 2.6 类型安全连接

为了避免魔术字符串（"department"），如果您的模型类可访问，请使用方法引用。

```java
userService.queryChain()
    .join(Join.inner(User::getDepartment))
    .where(c -> c.eq(User::getName, "Alice"))
    .exec();
```

---

