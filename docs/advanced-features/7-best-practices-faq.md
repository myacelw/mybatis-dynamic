# 7. Best Practices & FAQ

### 6.1 Transaction Management
`mybatis-dynamic` integrates seamlessly with Spring's transaction management.

- **Usage**: Simply add `@Transactional` to your Service methods.
- **Mechanism**: The framework's `DataManager` obtains the `SqlSession` from Spring's `SqlSessionUtils`, ensuring it participates in the current active transaction (propagation, rollback, etc.).

```java
@Service
public class UserService extends BaseService<Integer, User> {
    
    @Transactional(rollbackFor = Exception.class)
    public void createUserWithProfile(User user, Profile profile) {
        this.insert(user);           // Uses Transaction A
        profileService.insert(profile); // Uses Transaction A
        // If query fails here, both inserts roll back.
    }
}
```

### 6.2 Thread Safety
- **`BaseService` / `BaseDao`**: These are singletons in Spring (Scope: Singleton). They are thread-safe because they do not hold conversational state.
- **`DataManager`**: The default implementation is designed to be lightweight. When accessed via `BaseService`, it ensures the underlying `SqlSession` is thread-bound (via `ThreadLocal` in Spring).
- **Chains (`QueryChain`, etc.)**: These are **NOT** thread-safe. They are stateful objects created per request.
  - **Correct**: Create, configure, and execute in one method scope.
  - **Incorrect**: Storing a `QueryChain` instance in a class field and reusing it across threads.

### 6.3 Performance Tips
1.  **Select What You Need**: Use `.queryChain().select(...)` rather than fetching `SELECT *`, especially if tables have huge text columns.
2.  **Batch Operations**: Always use `.batchInsert()` or `.batchUpdate()` for lists > 50 items.
3.  **Indices**: Use the `@BasicField(ddlIndex = true)` annotation to ensure your `where` clauses are backed by DB indices. The framework handles index creation for you.
