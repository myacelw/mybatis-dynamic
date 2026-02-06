# 核心功能

本指南涵盖了 `mybatis-dynamic` 的基础功能，使你能够执行标准的 CRUD 操作、管理模型生命周期以及实施安全策略。

## 1. 简介

`mybatis-dynamic` 是一个基于 MyBatis 构建的运行时动态 ORM。与依赖静态 XML 或编译时类的传统 ORM 不同，它允许你：

1.  **在运行时定义/修改模型**：你无需重启应用程序即可创建新表或添加列。
2.  **代码优先（Code-First）DDL**：框架自动将你的 Java 模型变更同步到数据库模式。
3.  **统一的数据 API**：使用 `DataManager` 跨所有实体进行一致、流畅的 API 操作。

---

## 2. DataManager `<ID>`

`DataManager` 是执行所有 CRUD 操作的 **主要接口**。你需要获取特定模型（例如 `User`）的 `DataManager` 实例，并使用它来操作数据。

**泛型类型：** `<ID>` 代表主键的类型（例如 `Integer`、`String`、`Long`）。

### 2.1 插入操作

用于添加新记录的方法。

#### `insert(Object data)`
*   **功能**：插入单条记录。如果主键为 null，框架将尝试生成它（基于 `@IdField` 配置）。
*   **参数**：`data` - 实体对象或 `Map<String, Object>`。
*   **返回值**：`ID` - 生成或插入的主键。
*   **示例**：
    ```java
    User user = new User();
    user.setName("Alice");
    Integer id = userManager.insert(user);
    ```

#### `insertDisableGenerateId(Object data)`
*   **功能**：使用 **提供的** 主键插入记录，跳过任何自动生成逻辑。用于数据迁移或当你必须控制 ID 时。
*   **参数**：`data` - 已设置 ID 字段的对象。
*   **返回值**：`ID` - 插入的 ID。
*   **示例**：
    ```java
    User user = new User();
    user.setId(1001); // 手动 ID
    user.setName("System Admin");
    userManager.insertDisableGenerateId(user);
    ```

#### `batchInsert(List<?> data)`
*   **功能**：在单个批次中插入多条记录。使用特定于数据库的语法进行优化（例如 `INSERT INTO ... VALUES (...), (...)`）。
*   **参数**：`data` - 实体或 Map 的列表。
*   **返回值**：`List<ID>` - 生成的 ID 列表。
*   **示例**：
    ```java
    List<User> users = Arrays.asList(new User("Bob"), new User("Charlie"));
    userManager.batchInsert(users);
    ```

#### `insertOrUpdate(Object data)`
*   **功能**：如果 ID 不存在（或基于唯一键），则插入记录；如果存在，则更新记录（Upsert）。
*   **参数**：`data` - 实体或 Map。
*   **返回值**：`ID`。

---

### 2.2 更新操作

用于修改现有记录的方法。

#### `update(ID id, Object data)`
*   **功能**：更新由 `id` 标识的记录。默认情况下 **仅更新** `data` 对象中的 **非空字段**。
*   **参数**：
    *   `id`：目标主键。
    *   `data`：包含要更新字段的对象。
*   **示例**：
    ```java
    User updateInfo = new User();
    updateInfo.setAge(30);
    // 仅更新用户 1001 的 'age' 字段。'name' 保持不变。
    userManager.update(1001, updateInfo);
    ```

#### `update(Object data)`
*   **功能**：同上，但从 `data` 对象本身提取 ID。
*   **参数**：`data` - 必须包含 ID 字段。

#### `updateOnlyNonNull(Object data)`
*   **功能**：显式地仅更新非空字段。类似于 `update(Object data)`，但强调部分更新行为。
*   **参数**：`data` - 带有 ID 的实体/Map。

#### `updateByCondition(Consumer<ConditionBuilder> condition, Object data, boolean updateOnlyNonNull)`
*   **功能**：基于 `WHERE` 子句的批量更新。
*   **参数**：
    *   `condition`：构建 WHERE 子句的 Lambda 表达式。
    *   `data`：包含新值的对象。
    *   `updateOnlyNonNull`：如果为 `true`，则忽略 `data` 中的 null 字段。
*   **返回值**：`int` - 受影响的行数。
*   **示例**：
    ```java
    User template = new User();
    template.setStatus("Inactive");
    // 将所有年龄大于 60 的用户状态设置为 'Inactive'
    userManager.updateByCondition(c -> c.gt("age", 60), template, true);
    ```

#### `updateChain()` / `updateByConditionChain()`
*   **功能**：为复杂的更新场景提供流畅的构建器。
*   **示例**：
    ```java
    userManager.updateChain()
        .id(1001)
        .data(user)
        .updateOnlyNonNull() // 或 .allowNull()
        .exec();
    ```

---

### 2.3 删除操作

#### `delete(ID id)` / `delete(ID id, boolean forcePhysicalDelete)`
*   **功能**：按 ID 删除记录。
*   **行为**：
    *   如果存在 `@Model(logicDelete = true)`，则执行 **逻辑删除**（UPDATE set del_flag=1）。
    *   如果 `forcePhysicalDelete` 为 `true`，无论模型设置如何，都会执行 **物理删除**（DELETE FROM ...）。
*   **返回值**：`boolean` - 如果成功则为 True。

#### `delete(Consumer<ConditionBuilder> condition)`
*   **功能**：基于条件的批量删除。
*   **示例**：
    ```java
    // 删除所有非活跃用户
    userManager.delete(c -> c.eq("status", "Inactive"));
    ```

#### `batchDelete(Collection<ID> idList)`
*   **功能**：通过 ID 批量删除多条记录。
*   **返回值**：`int` - 删除的记录数。

---

### 2.4 查询（Select）操作

框架区分返回 **实体（Entities）**（类型安全）和 **Map**（动态）。

#### `getById(ID id, Class<T> clazz)`
*   **功能**：按主键精确查找。
*   **返回值**：类型为 `T` 的实体或 `null`。
*   **示例**：`User user = userManager.getById(1, User.class);`

#### `getByIds(Collection<ID> ids, Class<T> clazz)`
*   **功能**：按 ID 批量查找。
*   **返回值**：`List<T>`。

#### `query(Consumer<ConditionBuilder> condition)`
*   **功能**：最常用的查询方法。返回 **Map** 列表（`List<Map<String, Object>>`）。
*   **参数**：`condition` - WHERE 子句构建器。
*   **示例**：
    ```java
    List<Map<String, Object>> list = userManager.query(c -> c.like("name", "A%"));
    ```

#### `queryChain(Class<T> clazz)`
*   **功能**：启动一个返回实体的流畅查询构建器。
*   **能力**：
    *   `.where(...)`：添加条件。
    *   `.select(...)`：指定列。
    *   `.orderBy(...)`：排序。
    *   `.joins(...)`：配置 JOIN。
*   **示例**：
    ```java
    List<User> admins = userManager.queryChain(User.class)
        .select("id", "name", "email")
        .where(c -> c.eq("role", "ADMIN")
                     .gt("loginCount", 5))
        .orderBy("createTime desc")
        .exec();
    ```

#### `page(Class<T> clazz, int pageCurrent, int pageSize, Consumer<ConditionBuilder> condition)`
*   **功能**：标准分页。
*   **返回值**：包含 `.getList()` 和 `.getTotal()` 的 `PageResult<T>`。
*   **示例**：
    ```java
    PageResult<User> page = userManager.page(User.class, 1, 10, c -> c.eq("status", 1));
    long total = page.getTotal();
    List<User> list = page.getList();
    ```

---

### 2.5 高级数据操作

#### `queryCursor(Class<T> clazz, Consumer<ConditionBuilder> condition)`
*   **功能**：**流式查询**。返回 MyBatis 的 `Cursor`。
*   **场景**：处理 100 万条记录而无需将所有记录加载到内存中（防止 OOM）。
*   **用法**：必须在 `@Transactional` 块中使用。
*   **示例**：
    ```java
    try (Cursor<User> cursor = userManager.queryCursorChain(User.class).exec()) {
        for (User user : cursor) {
            process(user); // 逐条处理
        }
    }
    ```

#### `aggQuery(Class<T> clazz)`
*   **功能**：**聚合**。执行 `COUNT`、`SUM`、`AVG`、`MAX`、`MIN`。
*   **示例**：
    ```java
    // 计算每个部门的统计数据
    List<DeptStats> stats = userManager.aggQuery(DeptStats.class)
        .groupBy("deptId")     // GROUP BY dept_id
        .count("id", "count")  // COUNT(id) as count
        .avg("salary", "avg")  // AVG(salary) as avg
        .exec();
    ```

#### `getRecursiveTreeById(ID id)`
*   **功能**：递归获取节点及其 **所有子节点**（树结构）。
*   **场景**：获取特定分类及其所有子分类。
*   **前提条件**：模型必须是树模型（继承 `BaseTreeEntity` 或配置了 parent-id）。

---

## 3. ModelService

`ModelService` 管理模型本身的生命周期，而不仅仅是数据。

#### `register(Model model)`
*   **功能**：在运行时注册一个新的动态模型。
*   **场景**：用户在 UI 中定义一个新的“表单” -> 你创建一个 `Model` 对象 -> 注册它 -> 框架创建表。

#### `update(Model model)`
*   **功能**：更新数据库模式（DDL）以匹配提供的模型。
*   **行为**：
    *   添加缺失的列。
    *   更新列类型/注释。
    *   创建/更新索引。
    *   **安全**：它通常 *不会* 删除列以防止数据丢失。

#### `getDataManager(Class<?> entityClass)`
*   **功能**：获取用于业务逻辑的 `DataManager` 的工厂方法。
*   **示例**：
    ```java
    DataManager<Long> dm = modelService.getDataManager(Product.class);
    dm.insert(new Product("Phone"));
    ```

---

## 4. DataChangeInterceptor

此接口允许你挂钩数据生命周期。用于审计、验证和事件触发。

### 使用场景：全局审计日志
你希望记录对数据库的每一次更改。

### 方法
*   **`beforeInsert(DataManager, data)`**：插入前修改数据（例如，设置默认 UUID）。
*   **`afterInsert(DataManager, data, id)`**：记录已创建记录。
*   **`beforeUpdate(DataManager, id, data, fieldValues)`**：检查用户是否允许更新特定字段。
*   **`afterUpdate(...)`**：记录“用户 X 将字段 Y 从 A 更改为 B”。
*   **`beforePhysicalDelete` / `beforeLogicDelete`**：防止删除受保护的记录。

### 实现示例
```java
@Component
public class AuditInterceptor implements DataChangeInterceptor {

    @Override
    public void afterUpdate(DataManager<Object> dm, Object id, Object data, List<FieldValue> fieldValues) {
        String tableName = dm.getModel().getTableName();
        log.info("Table [{}] Record [{}] updated.", tableName, id);
        
        // 记录变更的字段
        for (FieldValue fv : fieldValues) {
             log.info("Field [{}] changed to [{}]", fv.getFieldName(), fv.getValue());
        }
    }
}
```
**注册**：只需将其定义为 Spring `@Component`。框架会自动检测它。

---

## 5. PermissionGetter & CurrentUserHolder

这是安全引擎。它拦截 **每一个** 数据库查询以注入安全约束。

### 5.1 接口：`CurrentUserHolder`
在 Spring 应用程序中，你实现此接口以将你的安全上下文（Spring Security/Shiro）与 ORM 连接起来。

#### `getCurrentUserId()`
*   **用例**：框架自动使用此 ID 填充 `@BasicField(fill=FillType.INSERT_USER)` 字段。

#### `getCurrentUserPermission(Model model)`
*   **用例**：返回被访问特定模型的权限。
*   **返回类型**：`Permission` 对象。

### 5.2 `Permission` 对象
由两个强大的约束组成：

1.  **`dataRights` (Condition)**：**行级安全**。
    *   此条件被 **AND** 到每个 `SELECT`、`UPDATE` 和 `DELETE` 查询中。
    *   *示例*：`tenant_id = 'T001'` 确保用户永远不会看到来自其他租户的数据。

2.  **`fieldRights` (List\<String>)**：**列级安全**。
    *   如果非空，`SELECT` 语句中将仅返回这些字段。
    *   用于从通用 UI 端点隐藏敏感字段，如 `salary` 或 `password_hash`。

### 5.3 完整的安全实现

```java
@Component
public class MySecurityHolder implements CurrentUserHolder {

    @Override
    public String getCurrentUserId() {
        // 1. 从安全上下文获取用户 ID
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public Permission getCurrentUserPermission(Model model) {
        String userId = getCurrentUserId();
        String role = getUserRole(userId); // 假设我们有这个方法

        // 超级管理员：无限制
        if ("ADMIN".equals(role)) {
            return null; 
        }

        // 场景：“订单”表的多租户
        if ("Order".equals(model.getName())) {
            String tenantId = getUserTenant(userId);
            
            // 行级安全：强制 tenant_id 检查
            Condition rowScope = SimpleCondition.eq("tenant_id", tenantId);
            
            return new Permission(null, rowScope);
        }

        // 场景：员工表可见性
        if ("Employee".equals(model.getName())) {
            // 列级安全：普通用户看不到 'salary'
            List<String> allowedFields = Arrays.asList("id", "name", "department");
            
            return new Permission(allowedFields, null);
        }

        return null; // 默认：无访问权限或完全访问权限（取决于你的策略）
    }
}
```
