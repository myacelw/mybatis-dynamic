# API Cheatsheet & Reference

## 1. Model Definition Annotations
| Annotation | Target | Description | Key Attributes |
|:---|:---|:---|:---|
| `@Model` | Class | Marks entity. | `tableName`, `comment`, `logicDelete`, `disableTableCreateAndAlter` |
| `@IdField` | Field | Primary Key. | `keyGeneratorMode` (UUID/SNOWFLAKE/IDENTITY), `order` (for composite keys) |
| `@BasicField` | Field | Column Config. | `columnName`, `ddlNotNull`, `ddlDefaultValue`, `ddlIndex`, `typeHandler` |
| `@ToOne` | Field | N:1 or 1:1. | `targetModel`, `joinLocalFields` (FK in this table) |
| `@ToMany` | Field | 1:N. | `targetModel`, `joinTargetFields` (FK in target table) |
| `@IgnoreField`| Field | Not a column. | - |

## 2. Core DataManager API
Obtain via `modelService.getDataManager(User.class)` or inject `BaseService<ID, T>`.

- **Insert**: `insert(entity)`, `batchInsert(list)`, `insertOrUpdate(entity)`.
- **Update**: `update(id, entity)` (updates non-nulls), `updateByCondition(condition, entity, ignoreNulls)`.
- **Delete**: `delete(id)`, `delete(condition)`, `batchDelete(ids)`.
- **Select**: `getById(id, Class)`, `query(condition)` (returns Maps).

## 3. ConditionBuilder (Fluent WHERE)
Used in `.where(c -> c...)`. Supports implicit `AND`.

- **Comparison**: `.eq`, `.ne`, `.gt`, `.gte`, `.lt`, `.lte`
- **String**: `.like`, `.startsWith`, `.endsWith`, `.contains`
- **List**: `.in`, `.notIn`
- **Null**: `.isNull`, `.isNotNull`
- **Logical**: `.or()`, `.bracket(b -> ...)`
- **Subquery**: `.exists("relationField", sub -> ...)`
- **Dynamic**: `*Optional` variants (e.g., `.eqOptional`) ignore condition if value is null/empty.

## 4. Chain APIs
| Chain | Entry Point | Usage |
|:---|:---|:---|
| **QueryChain** | `.queryChain()` | Select specific cols, joins, sorting, paging. |
| **UpdateChain** | `.updateChain()` | Granular updates (`.updateOnlyNonNull()`, `.force()`). |
| **PageChain** | `.pageChain()` | Pagination wrapper (`.page(1, 10)`). |
| **Recursive** | `.queryRecursive*Chain()` | Tree traversal (`.initNodeCondition(...)`, `.recursiveDown(bool)`). |
| **Fill** | `.fillChain()` | Populate relations on existing objects. |

## 5. Advanced Features
### Recursive Queries
- **Tree**: `queryRecursiveTreeChain()` returns nested objects (`children` list).
- **List**: `queryRecursiveListChain()` returns flat list of all descendants/ancestors.

### Permissions (Security)
- **Row Level**: `Permission.dataRights` (e.g., `tenant_id = 'T1'`).
- **Col Level**: `Permission.fieldRights` (List of visible fields).

## 6. Configuration (application.yml)
```yaml
mybatis-dynamic:
  update-model: true       # Auto-DDL
  table-prefix: "t_"       # t_user
  rest:
    enabled: true          # /api/dynamic/{model}
```

## 7. Extension Points (SPI)
- **New Database Dialect**: Implement `AbstractDataBaseDialect`, register in `META-INF/services/...DataBaseDialect`.
- **Custom Commands**: Implement `Execution<ID, Result, Command>`, register in `META-INF/services/...Execution`.
