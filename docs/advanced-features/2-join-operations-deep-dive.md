# 2. Join Operations (Deep Dive)

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

