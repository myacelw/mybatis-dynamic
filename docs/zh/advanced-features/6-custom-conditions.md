# 6. 自定义条件 (Custom Conditions)

虽然 `eq`、`gt`、`like` 等内置条件涵盖了大多数场景，但您可能需要特定于数据库的函数或复杂表达式。`CustomCondition` 允许您安全地注入原生 SQL 片段。

### 6.1 概念
当您需要利用标准 API 不原生支持的特定数据库功能（例如 MySQL `MATCH AGAINST`、PostGIS 函数）时，请使用 `CustomCondition`。

### 6.2 用法
`CustomCondition` 类需要：
- **`sqlTemplate`**: SQL 片段。使用 `$COL` 作为列名占位符，`#{EXPR}` 作为值占位符。
- **`field`**: Java 字段名称（解析为数据库列）。
- **`value`**: 绑定到 `#{EXPR}` 占位符的值。

### 6.3 示例：MySQL 全文搜索
```java
// SQL: MATCH (content) AGAINST ('keyword' IN BOOLEAN MODE)
userService.query()
    .where(CustomCondition.of(
        "MATCH ($COL) AGAINST (#{EXPR} IN BOOLEAN MODE)", // 模板
        User.Fields.content,                              // 字段 ($COL)
        "mybatis"                                         // 值 (#{EXPR})
    ))
    .exec();
```

---

