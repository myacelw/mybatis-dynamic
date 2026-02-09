# 2. DataManager `<ID>`

`DataManager` is the **primary interface** for all CRUD operations. You obtain an instance of `DataManager` for a specific model (e.g., `User`) and use it to manipulate data.

**Generic Type:** `<ID>` represents the type of the Primary Key (e.g., `Integer`, `String`, `Long`).

### 2.1 Insert Operations

Methods for adding new records.

#### `insert(Object data)`
*   **Function**: Inserts a single record. If the Primary Key is null, the framework attempts to generate it (based on `@IdField` configuration).
*   **Parameters**: `data` - Entity object or `Map<String, Object>`.
*   **Returns**: `ID` - The generated or inserted Primary Key.
*   **Example**:
    ```java
    User user = new User();
    user.setName("Alice");
    Integer id = userManager.insert(user);
    ```

#### `insertDisableGenerateId(Object data)`
*   **Function**: Inserts a record using the **provided** Primary Key, bypassing any auto-generation logic. Use this for data migration or when you must control the ID.
*   **Parameters**: `data` - Object with the ID field already set.
*   **Returns**: `ID` - The inserted ID.
*   **Example**:
    ```java
    User user = new User();
    user.setId(1001); // Manual ID
    user.setName("System Admin");
    userManager.insertDisableGenerateId(user);
    ```

#### `batchInsert(List<?> data)`
*   **Function**: Inserts multiple records in a single batch. Optimized using database-specific syntax (e.g., `INSERT INTO ... VALUES (...), (...)`).
*   **Parameters**: `data` - List of Entities or Maps.
*   **Returns**: `List<ID>` - List of generated IDs.
*   **Example**:
    ```java
    List<User> users = Arrays.asList(new User("Bob"), new User("Charlie"));
    userManager.batchInsert(users);
    ```

#### `insertOrUpdate(Object data)`
*   **Function**: Inserts the record if the ID doesn't exist (or based on unique keys); updates it if it does (Upsert).
*   **Parameters**: `data` - Entity or Map.
*   **Returns**: `ID`.

---

### 2.2 Update Operations

Methods for modifying existing records.

#### `update(ID id, Object data)`
*   **Function**: Updates the record identified by `id`. **Only updates non-null fields** from the `data` object by default.
*   **Parameters**:
    *   `id`: The target Primary Key.
    *   `data`: Object containing fields to update.
*   **Example**:
    ```java
    User updateInfo = new User();
    updateInfo.setAge(30);
    // Updates only 'age' for User 1001. 'name' remains unchanged.
    userManager.update(1001, updateInfo);
    ```

#### `update(Object data)`
*   **Function**: Same as above, but extracts the ID from the `data` object itself.
*   **Parameters**: `data` - Must contain the ID field.

#### `updateOnlyNonNull(Object data)`
*   **Function**: Explicitly updates only non-null fields. Similar to `update(Object data)` but emphasizes the partial update behavior.
*   **Parameters**: `data` - Entity/Map with ID.

#### `updateByCondition(Consumer<ConditionBuilder> condition, Object data, boolean updateOnlyNonNull)`
*   **Function**: Bulk update based on a `WHERE` clause.
*   **Parameters**:
    *   `condition`: Lambda to build the WHERE clause.
    *   `data`: Object containing new values.
    *   `updateOnlyNonNull`: If `true`, ignores null fields in `data`.
*   **Returns**: `int` - Number of affected rows.
*   **Example**:
    ```java
    User template = new User();
    template.setStatus("Inactive");
    // Set status='Inactive' for all users older than 60
    userManager.updateByCondition(c -> c.gt("age", 60), template, true);
    ```

#### `updateChain()` / `updateByConditionChain()`
*   **Function**: Provides a fluent builder for complex update scenarios.
*   **Example**:
    ```java
    userManager.updateChain()
        .id(1001)
        .data(user)
        .updateOnlyNonNull() // or .allowNull()
        .exec();
    ```

---

### 2.3 Delete Operations

#### `delete(ID id)` / `delete(ID id, boolean forcePhysicalDelete)`
*   **Function**: Deletes a record by ID.
*   **Behavior**:
    *   If `@Model(logicDelete = true)` is present, this performs a **Logical Delete** (UPDATE set del_flag=1).
    *   If `forcePhysicalDelete` is `true`, it performs a **Physical Delete** (DELETE FROM ...) regardless of model settings.
*   **Returns**: `boolean` - True if successful.

#### `delete(Consumer<ConditionBuilder> condition)`
*   **Function**: Bulk delete based on criteria.
*   **Example**:
    ```java
    // Delete all inactive users
    userManager.delete(c -> c.eq("status", "Inactive"));
    ```

#### `batchDelete(Collection<ID> idList)`
*   **Function**: Deletes multiple records by their IDs.
*   **Returns**: `int` - Number of records deleted.

---

### 2.4 Query (Select) Operations

The framework distinguishes between returning **Entities** (Type-safe) and **Maps** (Dynamic).

#### `getById(ID id, Class<T> clazz)`
*   **Function**: precise lookup by Primary Key.
*   **Returns**: Entity of type `T` or `null`.
*   **Example**: `User user = userManager.getById(1, User.class);`

#### `getByIds(Collection<ID> ids, Class<T> clazz)`
*   **Function**: Batch lookup by IDs.
*   **Returns**: `List<T>`.

#### `query(Consumer<ConditionBuilder> condition)`
*   **Function**: The most common query method. Returns a list of **Maps** (`List<Map<String, Object>>`).
*   **Parameters**: `condition` - The WHERE clause builder.
*   **Example**:
    ```java
    List<Map<String, Object>> list = userManager.query(c -> c.like("name", "A%"));
    ```

#### `queryChain(Class<T> clazz)`
*   **Function**: Starts a fluent query builder returning Entities.
*   **Capabilities**:
    *   `.where(...)`: Add conditions.
    *   `.select(...)`: Specify columns.
    *   `.orderBy(...)`: Sorting.
    *   `.joins(...)`: Configure JOINs.
*   **Example**:
    ```java
    List<User> admins = userManager.queryChain(User.class)
        .select("id", "name", "email")
        .where(c -> c.eq("role", "ADMIN")
                     .gt("loginCount", 5))
        .orderBy("createTime desc")
        .exec();
    ```

#### `page(Class<T> clazz, int pageCurrent, int pageSize, Consumer<ConditionBuilder> condition)`
*   **Function**: Standard pagination.
*   **Returns**: `PageResult<T>` containing `.getList()` and `.getTotal()`.
*   **Example**:
    ```java
    PageResult<User> page = userManager.page(User.class, 1, 10, c -> c.eq("status", 1));
    long total = page.getTotal();
    List<User> list = page.getList();
    ```

---

### 2.5 Advanced Data Operations

#### `queryCursor(Class<T> clazz, Consumer<ConditionBuilder> condition)`
*   **Function**: **Streaming Query**. Returns a MyBatis `Cursor`.
*   **Scenario**: Processing 1 million records without loading all into memory (OOM protection).
*   **Usage**: Must be used within a `@Transactional` block.
*   **Example**:
    ```java
    try (Cursor<User> cursor = userManager.queryCursorChain(User.class).exec()) {
        for (User user : cursor) {
            process(user); // Process one by one
        }
    }
    ```

#### `aggQuery(Class<T> clazz)`
*   **Function**: **Aggregation**. Performs `COUNT`, `SUM`, `AVG`, `MAX`, `MIN`.
*   **Example**:
    ```java
    // Calculate stats per department
    List<DeptStats> stats = userManager.aggQuery(DeptStats.class)
        .groupBy("deptId")     // GROUP BY dept_id
        .count("id", "count")  // COUNT(id) as count
        .avg("salary", "avg")  // AVG(salary) as avg
        .exec();
    ```

#### `getRecursiveTreeById(ID id)`
*   **Function**: Recursively fetches a node and **all its children** (Tree structure).
*   **Scenario**: Fetching a specific category and all sub-categories.
*   **Prerequisite**: Model must be a Tree model (extends `BaseTreeEntity` or configured with parent-id).

---

