# 1. Introduction

`mybatis-dynamic` is a runtime-dynamic ORM built on MyBatis. Unlike traditional ORMs that rely on static XML or compile-time classes, it allows you to:
1.  **Define/Modify Models at Runtime**: You can create new tables or add columns without restarting the application.
2.  **Code-First DDL**: The framework automatically syncs your Java model changes to the database schema.
3.  **Unified Data API**: Use `DataManager` for a consistent, fluent API across all entities.

---

