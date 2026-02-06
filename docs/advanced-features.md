# Advanced Features

This guide deep dives into the powerful Chain APIs, Recursive Operations, and Relation Filling capabilities of `mybatis-dynamic`.

## 1. Fluent Chain APIs (Deep Dive)

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

## 2. Join Operations (Deep Dive)

Effective data retrieval often involves querying multiple related tables. `mybatis-dynamic` supports both implicit (automatic) and explicit (manual) join strategies.

### 2.1 Concept: @ToOne and @ToMany

The framework relies on model annotations to understand relationships:

- **`@ToOne`**: Represents `One-to-One` or `Many-to-One` relationships.
  - *Example*: A `User` belongs to a `Department`.
- **`@ToMany`**: Represents `One-to-Many` relationships.
  - *Example*: A `Department` has many `User`s.

These annotations tell the framework *how* to join tables (which columns act as foreign keys) without you needing to write `ON` clauses manually every time.

### 2.2 Implicit Joins (Automatic)

The simplest way to join is to just ask for the data. If you select a field from a `@ToOne` related entity, the framework automatically generates a **Left Join**.

```java
// "department" is a @ToOne field in User
userService.queryChain()
    .select(User.Fields.name, "department.name") // Selects user.name AND department.name
    .where(c -> c.eq("department.status", "ACTIVE")) // Adds filter on joined table
    .exec();
```
* **Mechanism**: The framework sees `department.` path, resolves the relationship from the `User` model, and appends `LEFT JOIN department ON user.department_id = department.id`.

### 2.3 Explicit Joins

For precise control over join types or conditions, use the `.join()` method in `QueryChain`.

#### Join Types
```java
userService.queryChain()
    // Standard Inner Join
    .join(Join.inner("department"))
    
    // Left Join (Default if type not specified)
    .join(Join.left("department"))
    
    // Right Join
    .join(Join.right("department"))
    .exec();
```

#### Custom ON Conditions
You can add extra constraints to the `ON` clause, which is often more efficient than putting them in the `WHERE` clause for Outer Joins.

```java
.join(Join.inner("department")
          .on(c -> c.eq("status", "ACTIVE")
                    .gt("createTime", someDate)))
```

### 2.4 Nested & Chained Joins

You can join across multiple levels of relationships using the dot notation.

```java
// User -> Department -> Company
userService.queryChain()
    .join(Join.of("department.company")) 
    .select(User.Fields.name, "department.company.name")
    .exec();
```
* This automatically joins `Department` first, then joins `Company` to `Department`.

### 2.5 Dynamic Joins (One-to-Many)

While `@ToOne` joins are common for fetching single related records, you can also join `@ToMany` collections.

```java
// Department -> Users
departmentService.queryChain()
    .join(Join.of("users")) // Joins the User table
    .where(c -> c.eq("users.status", "BANNED")) // Find departments having banned users
    .exec();
```
* **Note**: This may result in "duplicate" Department rows in the SQL result set (Cartesian product), which the framework handles when mapping back to objects.

### 2.6 Type-Safe Joins

To avoid magic strings ("department"), use method references if your model classes are accessible.

```java
userService.queryChain()
    .join(Join.inner(User::getDepartment))
    .where(c -> c.eq(User::getName, "Alice"))
    .exec();
```

---

## 3. Recursive & Tree Operations

`mybatis-dynamic` has built-in support for hierarchical data (Adjacency List pattern), such as Categories, Departments, or Menus.

### 3.1 Recursive List vs. Recursive Tree

- **`queryRecursiveList`**: Fetches the hierarchy but returns a **flattened list**.
  - Useful for: Searching, calculations, or when you just need all descendants/ancestors without the nested structure.
- **`queryRecursiveTree`**: Fetches the hierarchy and assembles it into a **nested tree structure**.
  - **Requirement**: Your entity class must have a field to hold children (e.g., `List<Node> children`).

### 3.2 Key Concepts

1.  **`initNodeCondition`**: Defines the "Starting Points" (Roots) of your recursion.
2.  **`recursiveDown`**: 
    - `true`: Find children, grandchildren, etc. (Downwards).
    - `false`: Find parents, grandparents, etc. (Upwards).

### 3.3 Example: Category Tree

**Model**:
```java
@Model
public class Category {
    @IdField private String id;
    private String parentId;
    private String name;
    
    // Container for children (not a DB column)
    @IgnoreField
    private List<Category> children;
}
```

**Scenario 1: Fetch Full Tree (Root -> Leaves)**

```java
// 1. Define roots: Categories with no parent (parentId is null/empty)
List<Category> tree = categoryService.queryRecursiveTreeChain()
    .initNodeCondition(c -> c.isNull("parentId").or().eq("parentId", ""))
    .exec();

// Result is a List of 'Root' categories, each containing their 'children' recursively.
```

**Scenario 2: Fetch Breadcrumbs (Leaf -> Root)**

```java
// 1. Start from a specific sub-category
// 2. recursiveDown(false) implies "Go Up" to find parents
List<Category> breadcrumbs = categoryService.queryRecursiveListChain()
    .initNodeCondition(c -> c.eq("id", "sub-cat-123"))
    .recursiveDown(false) // Look upwards
    .exec();

// Result: [SubCategory, ParentCategory, RootCategory] (Order depends on traversal)
```

---

## 4. Relation Filling (`FillDataChain`)

Sometimes you have a list of objects obtained from an external source (like Redis, or a manual MyBatis XML query) and you want to populate their `@ToOne` or `@ToMany` relationships without writing a loop. `FillDataChain` solves this efficiently.

**Entry Point**: `dataManager.fillChain()`

```java
// Assume we got this list from Redis, so 'department' field is null
List<User> users = cacheService.getUsers(); 

userService.fillChain()
    .data(users) // The targets to populate
    
    // 1. Simple fill: Populates the 'department' field
    .fillField("department")
    
    // 2. Complex fill: Populates 'roles', but only specific columns
    .fillField("roles", fieldBuilder -> {
        fieldBuilder.selectField("roleName") // Only fetch name
                    .selectField("roleCode");
    })
    
    .exec();

// Now 'users' have their department and roles populated.
```

---

## 5. ModelService Advanced Features

The `ModelService` interface provides system-level controls, particularly for the automatic DDL (Data Definition Language) generation mechanism.

### 5.1 Dry Run Mode (`isDryRun`)
If you want to inspect what SQL changes the framework *would* make to your database schema without actually applying them (safe mode), use Dry Run.

```java
// Enable Dry Run
modelService.setDryRun(true);

// Trigger model update (normally happens at startup)
modelService.update(User.class);

// Disable Dry Run for normal operation
modelService.setDryRun(false);
```

### 5.2 DDL Logging (`setLogPath`)
You can configure the framework to write the generated `CREATE TABLE` / `ALTER TABLE` statements to a file instead of (or in addition to) executing them. This is excellent for auditing schema changes.

```java
// Set the path for DDL logs
modelService.setLogPath("/var/logs/app/ddl-changes.sql");
```

---

## 6. Custom Conditions

While built-in conditions like `eq`, `gt`, `like` cover most scenarios, you might need database-specific functions or complex expressions. `CustomCondition` allows you to inject raw SQL fragments safely.

### 6.1 Concept
Use `CustomCondition` when you need to leverage database-specific features (e.g., MySQL `MATCH AGAINST`, PostGIS functions) that are not natively supported by the standard API.

### 6.2 Usage
The `CustomCondition` class requires:
- **`sqlTemplate`**: The SQL fragment. Use `$COL` as a placeholder for the column name and `#{EXPR}` for the value placeholder.
- **`field`**: The Java field name (which resolves to the database column).
- **`value`**: The value to bind to the `#{EXPR}` placeholder.

### 6.3 Example: MySQL Full Text Search
```java
// SQL: MATCH (content) AGAINST ('keyword' IN BOOLEAN MODE)
userService.query()
    .where(CustomCondition.of(
        "MATCH ($COL) AGAINST (#{EXPR} IN BOOLEAN MODE)", // Template
        User.Fields.content,                              // Field ($COL)
        "mybatis"                                         // Value (#{EXPR})
    ))
    .exec();
```

---

## 7. Best Practices & FAQ

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
