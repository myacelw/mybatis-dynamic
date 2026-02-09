# 1. Fluent Chain APIs (Deep Dive)

While helper methods like `.query()`, `.update()`, and `.insert()` cover common use cases, **Chain APIs** provide granular control over the execution process. They allow you to build complex SQL commands step-by-step before executing them.

You access these chains via the `DataManager` (or `BaseService` which delegates to it).

### 1.1 QueryChain
The `QueryChain` offers the most flexibility for data retrieval, allowing precise control over selected columns, joins, and sorting.

**Entry Point**: `dataManager.queryChain()`

```java
List<User> users = userService.queryChain()
    // 1. Select specific columns (SQL optimization)
    .select(User.Fields.id, User.Fields.name, "department.name") 
    
    // 2. Add Joins manually (if not using automatic @ToOne/@ToMany inference)
    .join(Join.inner("department")
              .on(c -> c.eq("active", true)))
    
    // 3. Filtering
    .where(c -> c.gt(User.Fields.age, 18))
    
    // 4. Sorting
    .asc(User.Fields.age)      // Order by age ASC
    .desc(User.Fields.createTime) // Then by create_time DESC
    
    // 5. Execute
    .exec();
```

- **`.select(String... fields)`**: Restricts the columns fetched from the database. Can include fields from joined tables (e.g., `"department.name"`).
- **`.joins(Join...)`**: Explicitly defines joins. Useful when you need specific join types (`INNER`, `LEFT`) or extra conditions on the JOIN clause itself.
- **`.ignoreLogicDelete()`**: If your model uses logical deletion, this forces the query to include deleted records.

### 1.2 UpdateChain
The `UpdateChain` allows you to customize how updates are applied, specifically regarding `null` values and "force" updates.

**Entry Point**: `dataManager.updateChain()`

```java
userService.updateChain()
    .id(userId)
    .data(userObj)
    
    // Strategy: Only update fields that are NOT NULL in 'userObj'
    .updateOnlyNonNull() 
    
    // Strategy: Force update even if data hasn't changed 
    // (useful to update 'updateTime' or version columns)
    .force()
    
    // Strategy: Explicitly ignore certain fields even if they are set
    .ignoreFields(User.Fields.password, User.Fields.salt)
    
    .exec();
```

- **`.updateOnlyNonNull()`**: By default, `update` might overwrite database values with `null` if the field in the provided object is null. This method prevents that.
- **`.allowNull()`** (Implicit default): The opposite of `updateOnlyNonNull`.
- **`.force()`**: Standard updates check if values actually changed to avoid unnecessary DB writes. `.force()` bypasses this check.

### 1.3 PageChain
Combines standard query capabilities with pagination.

**Entry Point**: `dataManager.pageChain()`

```java
PageResult<User> page = userService.pageChain()
    .page(1, 20) // Current Page, Page Size
    .where(c -> c.eq("status", "ACTIVE"))
    .desc(User.Fields.createTime)
    .exec();

// Result contains:
long total = page.getTotal();
List<User> list = page.getData();
```

---

