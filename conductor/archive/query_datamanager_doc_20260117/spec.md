# Specification: Query & DataManager Documentation Overhaul

## Overview
This track involves a comprehensive update to the project's documentation (`README.md` and `README_CN.md`). The focus is on detailing the advanced querying capabilities (auto-joins, complex conditions) and providing a clear overview of the `DataManager` interface. The documentation will be reorganized using a role-based structure to better guide users from basic setup to advanced usage.

## Functional Requirements

### 1. Advanced Query Documentation
- **Auto-Join Mechanism:**
    - Explain how selecting fields from associated tables (e.g., `user.department.name`) triggers automatic joins.
    - Document that the default join type is `LEFT JOIN`.
    - Provide instructions on how to configure specific join types and add additional conditions within a join.
- **Query Conditions:**
    - Comprehensive guide on `ConditionBuilder` capabilities.
    - Examples for **Simple Conditions** (eq, gt, like, etc.).
    - Usage of **Logical Operators** (`and`, `or`, `not`).
    - Documentation for **Exists Conditions** and **Custom Conditions**.

### 2. DataManager Interface Overview
- Provide a concise introduction to all key methods in the `DataManager` interface:
    - Standard CRUD (insert, update, delete, getById).
    - Batch Operations (`batchInsert`, `batchUpdate`, `batchUpdateByCondition`).
    - Hierarchical Queries (`queryRecursiveTree`, `getRecursiveTreeById`).
    - Transaction Management integration tips.

### 3. Documentation Reorganization
- Restructure the READMEs using a **Role-based approach**:
    - **Getting Started (Beginner):** Installation, basic model definition, and simple CRUD.
    - **Advanced Data Management (Power User):** Complex queries, auto-joins, batch operations, and recursive data.
    - **Expert Features (Architect):** Interceptors, fillers, multi-tenancy, and custom type handlers.

## Non-Functional Requirements
- **Clarity:** Use consistent terminology and provided verified code snippets.
- **Bilingual Support:** All updates must be applied to both `README.md` (English) and `README_CN.md` (Chinese).

## Acceptance Criteria
- [ ] README structure is reorganized into Beginner, Advanced, and Expert sections.
- [ ] Auto-join behavior and configuration are clearly explained with examples.
- [ ] All `ConditionBuilder` operator types (Simple, Logical, Exists, Custom) are documented.
- [ ] `DataManager` API methods are summarized.
- [ ] All documentation remains consistent between English and Chinese versions.
