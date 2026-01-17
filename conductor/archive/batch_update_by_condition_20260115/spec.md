# Specification: BatchUpdateByCondition

## Overview
Implement a `BatchUpdateByCondition` feature to allow batch updates where each update operation can have its own independent condition and data. This requires a new execution path capable of handling multiple distinct SQL statements in a single batch operation.

## Functional Requirements

### 1. API & Command Structure
- **BatchUpdateByConditionCommand**: Defines the command object.
    - Fields: `List<UpdatePair> updates`.
    - `UpdatePair` holds the `Condition` and the `data` object.
- **BatchUpdateByConditionChain**: A fluent builder implementing the user interface.
    - `.add(Condition condition, Object data)`
    - `.add(Consumer<ConditionBuilder> conditionBuilder, Object data)`
    - `.exec()`: Builds the command and triggers execution.
- **BatchUpdateByConditionExecution**: Implements the `Execution` interface.
    - Responsible for translating the `BatchUpdateByConditionCommand` into SQL and Contexts.
    - Resolves the SQL for *each* pair individually (allowing for varying conditions).

### 2. Core Integration
- **MybatisHelper Integration**:
    - **Requirement:** Update `MybatisHelper` and `MybatisHelperImpl` to support a new method (e.g., `batchUpdates`) that accepts **multiple SQL statements** and their corresponding contexts.
    - This allows the execution of a batch where every item potentially has a different `WHERE` clause structure.
- **DataManager**: Add `BatchUpdateByConditionChain batchUpdateByConditionChain()` to the interface.
- **BaseDao**: Add convenience methods if applicable (e.g., `batchUpdateByCondition`).

### 3. Execution Logic
- Iterate through the `List<UpdatePair>`.
- For each pair, generate the specific update SQL based on its `Condition` and `Data`.
- Collect all generated SQLs and Contexts.
- Pass the lists to the new `MybatisHelper.batchUpdates` method for execution.

### 4. Events and Interceptors
- **Events**: Fire `BatchUpdateByConditionDataEvent` containing the list of pairs.
- **Interceptors**:
    - `beforeBatchUpdateByCondition`
    - `afterBatchUpdateByCondition`

## Non-Functional Requirements
- **Flexibility**: Must support batches where item A uses `WHERE id=?` and item B uses `WHERE name=?`.

## Acceptance Criteria
- [ ] `BatchUpdateByConditionChain` allows fluent construction of mixed-condition updates.
- [ ] `MybatisHelper` successfully executes a batch of differing SQL statements.
- [ ] `DataManager` exposes the new chain.
- [ ] Integration tests verify that multiple updates with different conditions are committed correctly.
