# Implementation Plan: BatchUpdateByCondition

## Phase 1: Foundations [checkpoint: cc0df89]
- [x] Task: Implement Command and Event classes
    - [ ] Create `BatchUpdateByConditionCommand` and `UpdatePair` in `core`
    - [ ] Create `BatchUpdateByConditionDataEvent` in `core`
- [x] Task: Extend `MybatisHelper` for Multi-SQL Batching
    - [ ] Add `batchUpdates(List<String> sqls, List<Object> contexts, int batchSize)` to `MybatisHelper` interface
    - [ ] Implement `batchUpdates` in `MybatisHelperImpl` to handle heterogeneous SQL statements in a single transaction/session
- [x] Task: Conductor - User Manual Verification 'Foundations' (Protocol in workflow.md)

## Phase 2: Execution Engine
- [ ] Task: Implement `BatchUpdateByConditionExecution`
    - [ ] Create `BatchUpdateByConditionExecution` class in `core`
    - [ ] Implement `exec` logic: loop through updates, generate SQL/Context for each, and call `MybatisHelper.batchUpdates`
- [ ] Task: Register Execution Service
    - [ ] Add `BatchUpdateByConditionExecution` to `META-INF/services/io.github.myacelw.mybatis.dynamic.core.service.execution.Execution`
- [ ] Task: Add Interceptor Hooks
    - [ ] Add `beforeBatchUpdateByCondition` and `afterBatchUpdateByCondition` to `DataChangeInterceptor`
    - [ ] Update `DataChangeInterceptorGroup` to delegate these new hooks
- [ ] Task: Conductor - User Manual Verification 'Execution Engine' (Protocol in workflow.md)

## Phase 3: Fluent API & Integration
- [ ] Task: Implement `BatchUpdateByConditionChain`
    - [ ] Create `BatchUpdateByConditionChain` in `core` supporting `.add(condition, data)` and `.add(builder, data)`
- [ ] Task: Integrate with `DataManager`
    - [ ] Add `batchUpdateByConditionChain()` to `DataManager` and its implementation
- [ ] Task: Integrate with Spring Module
    - [ ] Add `batchUpdateByCondition` methods to `BaseDao` in `spring` module
    - [ ] Add `batchUpdateByCondition` methods to `BaseService` in `spring` module
- [ ] Task: Conductor - User Manual Verification 'Fluent API & Integration' (Protocol in workflow.md)

## Phase 4: Final Verification
- [ ] Task: Comprehensive Integration Testing
    - [ ] Create integration tests in `sample` or `core` tests verifying mixed-condition batch updates
    - [ ] Verify interceptors and events are fired correctly
- [ ] Task: Conductor - User Manual Verification 'Final Verification' (Protocol in workflow.md)
