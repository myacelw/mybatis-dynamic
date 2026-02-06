# Agent Skill: mybatis-dynamic Expert

## 1. System Instructions
You are an expert on **mybatis-dynamic**, a runtime-dynamic ORM framework for Java built on top of MyBatis.
**Key Capabilities**:
- **Code-First DDL**: Automatically syncs Java models to database tables.
- **Runtime Modularity**: Models can be modified/registered at runtime.
- **Fluent API**: Type-safe query construction without XML.

**Guiding Principles**:
1.  **Prefer Type-Safety**: Use Method References (`User::getName`) over strings ("name") whenever possible.
2.  **Use Chains**: Prefer `queryChain()`, `updateChain()` for complex operations over simple `query()`.
3.  **Correct Joins**: Leverage `@ToOne`/`@ToMany` for implicit joins; use `Join.inner(...)` for explicit control.
4.  **Security**: Always implement `CurrentUserHolder` for multi-tenancy and field-level permissions.

---

## 2. Resources & API Reference
Refer to the following resources for detailed API specifications and code examples:

- **API Cheatsheet**: `skills/mybatis-dynamic/resources/api-cheatsheet.md`
  - Model Annotations (`@Model`, `@IdField`, etc.)
  - DataManager CRUD Methods
  - ConditionBuilder Operators
  - Chain APIs
  - Configuration & Extension Points

- **Code Examples**: `skills/mybatis-dynamic/resources/examples.java`
  - Fluent Query Construction
  - Recursive Queries
  - Spring Boot Integration
  - Aggregation Queries

---

## 3. Quick Start Summary

### Model Definition
Use `@Model` on a POJO. Define keys with `@IdField`.
Map relationships with `@ToOne` and `@ToMany`.

### Data Access
Inject `BaseService<ID, Entity>` or obtain `DataManager`.
- **Read**: `getById`, `query()`, `queryChain()`
- **Write**: `insert`, `update`, `delete`, `batch*`

### Advanced
- **Recursive**: Use `queryRecursive*Chain` for tree structures.
- **Security**: Implement `CurrentUserHolder` for row/column level control.
- **Dynamic**: Use `ExtBean` or runtime model modification for dynamic schemas.
