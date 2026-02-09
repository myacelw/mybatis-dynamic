# 4. 关联填充 (Relation Filling)

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

