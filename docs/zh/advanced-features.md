# 高级功能 (Advanced Features)

本指南深入探讨 `mybatis-dynamic` 的链式 API (Chain APIs)、递归操作 (Recursive Operations) 和关联填充 (Relation Filling) 功能。

## 1. 流畅链式 API (Fluent Chain APIs)

虽然像 `.query()`、`.update()` 和 `.insert()` 这样的辅助方法涵盖了常见用例，但 **链式 API** 提供了对执行过程的更细粒度控制。它们允许您在执行之前逐步构建复杂的 SQL 命令。

您可以通过 `DataManager`（或委托给它的 `BaseService`）访问这些链。

### 1.1 QueryChain
`QueryChain` 为数据检索提供了最大的灵活性，允许精确控制选定的列、连接和排序。

**入口点**: `dataManager.queryChain()`

```java
List<User> users = userService.queryChain()
    // 1. 选择特定列 (SQL 优化)
    .select(User.Fields.id, User.Fields.name, "department.name") 
    
    // 2. 手动添加连接 (如果未使用自动 @ToOne/@ToMany 推断)
    .join(Join.inner("department")
              .on(c -> c.eq("active", true)))
    
    // 3. 过滤
    .where(c -> c.gt(User.Fields.age, 18))
    
    // 4. 排序
    .asc(User.Fields.age)      // 按年龄升序
    .desc(User.Fields.createTime) // 然后按创建时间降序
    
    // 5. 执行
    .exec();
```

- **`.select(String... fields)`**: 限制从数据库获取的列。可以包含连接表的字段（例如 `"department.name"`）。
- **`.joins(Join...)`**: 显式定义连接。当您需要特定的连接类型 (`INNER`, `LEFT`) 或在 JOIN 子句本身上有额外条件时很有用。
- **`.ignoreLogicDelete()`**: 如果您的模型使用逻辑删除，这将强制查询包含已删除的记录。

### 1.2 UpdateChain
`UpdateChain` 允许您自定义更新的应用方式，特别是关于 `null` 值和“强制”更新。

**入口点**: `dataManager.updateChain()`

```java
userService.updateChain()
    .id(userId)
    .data(userObj)
    
    // 策略：仅更新 'userObj' 中非 NULL 的字段
    .updateOnlyNonNull() 
    
    // 策略：即使数据未更改也强制更新 
    // (用于更新 'updateTime' 或版本列)
    .force()
    
    // 策略：即使设置了也显式忽略某些字段
    .ignoreFields(User.Fields.password, User.Fields.salt)
    
    .exec();
```

- **`.updateOnlyNonNull()`**: 默认情况下，如果提供的对象中的字段为 null，`update` 可能会用 `null` 覆盖数据库值。此方法可防止这种情况。
- **`.allowNull()`** (隐式默认): 与 `updateOnlyNonNull` 相反。
- **`.force()`**: 标准更新会检查值是否实际更改以避免不必要的 DB 写入。`.force()` 绕过此检查。

### 1.3 PageChain
结合了标准查询功能和分页。

**入口点**: `dataManager.pageChain()`

```java
PageResult<User> page = userService.pageChain()
    .page(1, 20) // 当前页, 页面大小
    .where(c -> c.eq("status", "ACTIVE"))
    .desc(User.Fields.createTime)
    .exec();

// 结果包含:
long total = page.getTotal();
List<User> list = page.getData();
```

---

## 2. 连接操作 (Join Operations)

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

## 3. 递归与树形结构操作 (Recursive & Tree Operations)

`mybatis-dynamic` 内置支持分层数据（邻接表模式），例如类别、部门或菜单。

### 3.1 递归列表 vs. 递归树

- **`queryRecursiveList`**: 获取层级结构但返回 **扁平列表**。
  - 适用于：搜索、计算，或者当您只需要所有后代/祖先而不需要嵌套结构时。
- **`queryRecursiveTree`**: 获取层级结构并将其组装成 **嵌套树结构**。
  - **要求**: 您的实体类必须有一个字段来保存子节点（例如 `List<Node> children`）。

### 3.2 关键概念

1.  **`initNodeCondition`**: 定义递归的“起始点”（根）。
2.  **`recursiveDown`**: 
    - `true`: 查找子节点、孙节点等（向下）。
    - `false`: 查找父节点、祖父节点等（向上）。

### 3.3 示例: 类别树

**模型**:
```java
@Model
public class Category {
    @IdField private String id;
    private String parentId;
    private String name;
    
    // 子节点容器 (非 DB 列)
    @IgnoreField
    private List<Category> children;
}
```

**场景 1: 获取完整树 (Root -> Leaves)**

```java
// 1. 定义根: 没有父级的类别 (parentId 为 null/empty)
List<Category> tree = categoryService.queryRecursiveTreeChain()
    .initNodeCondition(c -> c.isNull("parentId").or().eq("parentId", ""))
    .exec();

// 结果是 'Root' 类别的列表，每个类别递归包含其 'children'。
```

**场景 2: 获取面包屑 (Leaf -> Root)**

```java
// 1. 从特定子类别开始
// 2. recursiveDown(false) 意味着 "向上" 查找父级
List<Category> breadcrumbs = categoryService.queryRecursiveListChain()
    .initNodeCondition(c -> c.eq("id", "sub-cat-123"))
    .recursiveDown(false) // 向上查找
    .exec();

// 结果: [SubCategory, ParentCategory, RootCategory] (顺序取决于遍历)
```

---

## 4. 关联填充 (Relation Filling)

有时您有一组从外部来源（如 Redis 或手动 MyBatis XML 查询）获取的对象，并且希望填充它们的 `@ToOne` 或 `@ToMany` 关系，而无需编写循环。`FillDataChain` 可以高效地解决这个问题。

**入口点**: `dataManager.fillChain()`

```java
// 假设我们从 Redis 获取了这个列表，所以 'department' 字段为 null
List<User> users = cacheService.getUsers(); 

userService.fillChain()
    .data(users) // 要填充的目标
    
    // 1. 简单填充: 填充 'department' 字段
    .fillField("department")
    
    // 2. 复杂填充: 填充 'roles'，但仅限特定列
    .fillField("roles", fieldBuilder -> {
        fieldBuilder.selectField("roleName") // 仅获取 name
                    .selectField("roleCode");
    })
    
    .exec();

// 现在 'users' 已填充了部门和角色。
```

---

## 5. ModelService 高级功能

`ModelService` 接口提供系统级控制，特别是针对自动 DDL（数据定义语言）生成机制。

### 5.1 空运行模式 (`isDryRun`)
如果您想检查框架 *将要* 对数据库 schema 进行哪些 SQL 更改，而不实际应用它们（安全模式），请使用空运行。

```java
// 启用空运行
modelService.setDryRun(true);

// 触发模型更新 (通常发生在启动时)
modelService.update(User.class);

// 禁用空运行以进行正常操作
modelService.setDryRun(false);
```

### 5.2 DDL 日志记录 (`setLogPath`)
您可以配置框架将生成的 `CREATE TABLE` / `ALTER TABLE` 语句写入文件，而不是（或除了）执行它们。这非常适合审计架构更改。

```java
// 设置 DDL 日志路径
modelService.setLogPath("/var/logs/app/ddl-changes.sql");
```

---

## 6. 自定义条件 (Custom Conditions)

虽然 `eq`、`gt`、`like` 等内置条件涵盖了大多数场景，但您可能需要特定于数据库的函数或复杂表达式。`CustomCondition` 允许您安全地注入原生 SQL 片段。

### 6.1 概念
当您需要利用标准 API 不原生支持的特定数据库功能（例如 MySQL `MATCH AGAINST`、PostGIS 函数）时，请使用 `CustomCondition`。

### 6.2 用法
`CustomCondition` 类需要：
- **`sqlTemplate`**: SQL 片段。使用 `$COL` 作为列名占位符，`#{EXPR}` 作为值占位符。
- **`field`**: Java 字段名称（解析为数据库列）。
- **`value`**: 绑定到 `#{EXPR}` 占位符的值。

### 6.3 示例：MySQL 全文搜索
```java
// SQL: MATCH (content) AGAINST ('keyword' IN BOOLEAN MODE)
userService.query()
    .where(CustomCondition.of(
        "MATCH ($COL) AGAINST (#{EXPR} IN BOOLEAN MODE)", // 模板
        User.Fields.content,                              // 字段 ($COL)
        "mybatis"                                         // 值 (#{EXPR})
    ))
    .exec();
```

---

## 7. 最佳实践与常见问题 (Best Practices & FAQ)

### 7.1 事务管理
`mybatis-dynamic` 与 Spring 的事务管理无缝集成。

- **用法**: 只需将 `@Transactional` 添加到您的 Service 方法。
- **机制**: 框架的 `DataManager` 从 Spring 的 `SqlSessionUtils` 获取 `SqlSession`，确保它参与当前活动事务（传播、回滚等）。

```java
@Service
public class UserService extends BaseService<Integer, User> {
    
    @Transactional(rollbackFor = Exception.class)
    public void createUserWithProfile(User user, Profile profile) {
        this.insert(user);           // 使用事务 A
        profileService.insert(profile); // 使用事务 A
        // 如果此处查询失败，两个插入都会回滚。
    }
}
```

### 7.2 线程安全
- **`BaseService` / `BaseDao`**: 这些在 Spring 中是单例（Scope: Singleton）。它们是线程安全的，因为它们不持有会话状态。
- **`DataManager`**: 默认实现设计为轻量级。通过 `BaseService` 访问时，它确保底层 `SqlSession` 是线程绑定的（通过 Spring 中的 `ThreadLocal`）。
- **链 (`QueryChain` 等)**: 这些 **不是** 线程安全的。它们是每个请求创建的有状态对象。
  - **正确**: 在一个方法范围内创建、配置和执行。
  - **错误**: 将 `QueryChain` 实例存储在类字段中并跨线程重用。

### 7.3 性能提示
1.  **按需选择**: 使用 `.queryChain().select(...)` 而不是获取 `SELECT *`，特别是如果表有巨大的文本列。
2.  **批量操作**: 对于 > 50 个项目的列表，始终使用 `.batchInsert()` 或 `.batchUpdate()`。
3.  **索引**: 使用 `@BasicField(ddlIndex = true)` 注解确保您的 `where` 子句由 DB 索引支持。框架会为您处理索引创建。
