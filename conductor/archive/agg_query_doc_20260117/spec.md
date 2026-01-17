# Specification: Aggregation Query Documentation

## Overview
This track involves adding comprehensive documentation for the `aggQuery` functionality to both `README.md` (English) and `README_CN.md` (Chinese). The documentation will cover how to perform aggregation queries, including specifying functions, grouping, filtering, and result mapping. A new section "Aggregation Queries" will be added under "Data Management".

## Functional Requirements

### 1. Document Aggregation Query Capabilities
- Create a new section **"Aggregation Queries"** under "Data Management".
- Explain the purpose of `aggQuery` for performing SQL aggregate functions (COUNT, SUM, AVG, MAX, MIN, etc.).

### 2. Detailed Usage Instructions
- **Basic Usage:** Show how to initiate an aggregation query using `dataManager.aggQuery()`.
- **Aggregation Functions:** Document how to use `count()`, `sum()`, `avg()`, `max()`, `min()`.
- **Grouping:** Document usage of `groupBy()` for categorizing results.
- **Filtering:**
    - `where()`: Filtering before aggregation.
    - `having()`: Filtering after aggregation.
- **Result Mapping:** Explain how to map results to a `Map<String, Object>` or a custom DTO class.

### 3. Code Examples
- Provide a complete code example that demonstrates:
    - Grouping by a field (e.g., `department_id`).
    - Calculating aggregates (e.g., `count(*)` and `avg(salary)`).
    - Filtering groups (e.g., having count > 5).

## Non-Functional Requirements
- **Bilingual:** Apply all changes to both English and Chinese documentation.
- **Consistency:** Maintain the existing formatting style of the README files.

## Acceptance Criteria
- [ ] A new "Aggregation Queries" section exists in `README.md`.
- [ ] A new "聚合查询" section exists in `README_CN.md`.
- [ ] Both sections cover basic usage, functions, grouping, filtering (where/having), and result mapping.
- [ ] Accurate code examples are provided in both languages.
