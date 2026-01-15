# Implementation Plan: BatchUpdateByCondition

## Phase 1: Foundations [checkpoint: cc0df89]
- [x] Task: Implement Command and Event classes
    - [ ] Create `BatchUpdateByConditionCommand` and `UpdatePair` in `core`
    - [ ] Create `BatchUpdateByConditionDataEvent` in `core`
- [x] Task: Extend `MybatisHelper` for Multi-SQL Batching
    - [ ] Add `batchUpdates(List<String> sqls, List<Object> contexts, int batchSize)` to `MybatisHelper` interface
    - [ ] Implement `batchUpdates` in `MybatisHelperImpl` to handle heterogeneous SQL statements in a single transaction/session
- [x] Task: Conductor - User Manual Verification 'Foundations' (Protocol in workflow.md)

## Phase 2: Execution Engine [checkpoint: a4dcbc4]
- [x] Task: Implement `BatchUpdateByConditionExecution`
    - [x] Create `BatchUpdateByConditionExecution` class in `core`
    - [x] Implement `exec` logic: loop through updates, generate SQL/Context for each, and call `MybatisHelper.batchUpdates`
- [x] Task: Register Execution Service
    - [x] Add `BatchUpdateByConditionExecution` to `META-INF/services/io.github.myacelw.mybatis.dynamic.core.service.execution.Execution`
- [x] Task: Add Interceptor Hooks
    - [x] Add `beforeBatchUpdateByCondition` and `afterBatchUpdateByCondition` to `DataChangeInterceptor`
    - [x] Update `DataChangeInterceptorGroup` to delegate these new hooks
- [x] Task: Conductor - User Manual Verification 'Execution Engine' (Protocol in workflow.md)

## Phase 3: Fluent API ## Phase 3: Fluent API & Integration Integration [checkpoint: 2130823]
- [x] Task: Implement `BatchUpdateByConditionChain`
    - [x] Create `BatchUpdateByConditionChain` in `core` supporting `.add(condition, data)` and `.add(builder, data)`
- [x] Task: Integrate with `DataManager`
    - [x] Add `batchUpdateByConditionChain()` to `DataManager` and its implementation
- [x] Task: Integrate with Spring Module
    - [x] Add `batchUpdateByCondition` methods to `BaseDao` in `spring` module
    - [x] Add `batchUpdateByCondition` methods to `BaseService` in `spring` module
- [x] Task: Conductor - User Manual Verification 'Fluent API - [~] Task: Conductor - User Manual Verification 'Fluent API & Integration' (Protocol in workflow.md) Integration' (Protocol in workflow.md)

## Phase 4: Final Verification
- [ ] Task: Comprehensive Integration Testing
    - [ ] Create integration tests in `sample` or `core` tests verifying mixed-condition batch updates
    - [ ] Verify interceptors and events are fired correctly
- [ ] Task: Conductor - User Manual Verification 'Final Verification' (Protocol in workflow.md)
