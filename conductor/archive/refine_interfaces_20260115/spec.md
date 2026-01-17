# Specification: Refine Database Operation Interfaces

## Overview
This track aims to refine and unify the database operation interfaces across `DataManager`, `BaseDao`, and `BaseService`. The goal is to provide a "better defined, unified, clear, and easy to use" API for developers, accompanied by comprehensive Javadoc and updated user guides.

## Functional Requirements

### 1. Interface Unification and Standardizing Naming
- **Standardize Update Naming**: Unify `updateOnlyNonNull` (DataManager) and `onlyUpdateNonNull` (BaseDao/BaseService). Recommendation: Use `updateOnlyNonNull` consistently across all layers.
- **Unify Count Return Types**: Ensure all `count` operations return `int` consistently.
- **Standardize Aggregate Operations**: Unify `aggQueryChain` (DataManager) and `agg` (BaseDao/BaseService). Recommendation: Use `agg` or `aggQuery` consistently.
- **Consistent Parameter Ordering**: Ensure that similar methods across different commands have a consistent order for `Condition`, `data`, and flags.

### 2. Interface Simplification and Overload Refinement
- Provide consistent overloads for `Condition` vs `Consumer<ConditionBuilder>`.
- Simplify `delete` and `batchDelete` overloads to be more intuitive across all interfaces.
- Ensure `BaseDao` and `BaseService` mirror `DataManager` capabilities for standard entity operations while maintaining appropriate higher-level abstractions.

### 3. Documentation and Comments Enhancement
- **Comprehensive Javadoc**: Add or improve Javadoc for every public method in `DataManager`, `BaseDao`, and `BaseService`. Documentation must include:
    - Clear description of the operation.
    - Parameter explanations.
    - Return value descriptions.
    - Usage examples where appropriate.
- **README Updates**: Update the project `README.md` to reflect the refined API patterns and provide updated "Best Practice" usage examples.

## Non-Functional Requirements
- **Simplicity & Maintainability**: Prioritize clean, idiomatic code over strict backward compatibility (project is pre-release). Breaking changes are acceptable if they improve the API.
- **Code Coverage**: Maintain >80% code coverage. All refactored interfaces must be verified by existing or new unit/integration tests.
- **API Consistency**: Ensure the fluent API chains (`queryChain`, `updateChain`, etc.) remain consistent with the new naming standards.

## Acceptance Criteria
- [ ] `DataManager`, `BaseDao`, and `BaseService` use unified naming conventions for all database operations.
- [ ] All `count` methods return `int`.
- [ ] Every public method in the target interfaces has clear, descriptive Javadoc.
- [ ] Method overloads are simplified and consistent across layers.
- [ ] `README.md` is updated with the latest API usage examples.
- [ ] All existing and new tests pass successfully.

## Out of Scope
- Implementation of new database dialect features.
- Refactoring of internal `Execution` logic (unless required to support interface changes like return type updates).
- UI changes in the `draw` module.
