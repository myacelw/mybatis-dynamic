# 6. Custom Conditions

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

