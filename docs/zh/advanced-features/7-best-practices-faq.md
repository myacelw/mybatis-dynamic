# 7. 最佳实践与常见问题 (Best Practices & FAQ)

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
