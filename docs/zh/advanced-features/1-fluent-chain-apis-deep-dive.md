# 1. 流畅链式 API (Fluent Chain APIs)

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

