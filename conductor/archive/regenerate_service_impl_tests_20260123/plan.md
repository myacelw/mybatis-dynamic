# Implementation Plan: Regenerate Service Implementation Tests

## Phase 1: Class2ModelTransferImpl Tests
This phase focuses on the mapping logic from Java classes to data models.

- [x] Task: Test Basic Field Mapping
    - [x] Simple types (String, int, Boolean, etc.)
    - [x] Enum types
    - [x] Map types
- [x] Task: Test Annotation-based Customization
    - [x] `@IdField` (Key generation, ordering)
    - [x] `@BasicField` (Column naming, selection, JDBC types)
    - [x] `@ExtProperty`
- [x] Task: Test Relationship Mapping
    - [x] `@ToOne` (Default and explicit joining)
    - [x] `@ToMany` (Collection types, target models)
    - [x] Automatic relationship detection for list types
- [x] Task: Test Complex Structures
    - [x] `@GroupField` (Embedded objects with prefixing)
    - [x] Inheritance (Fields from superclasses)
    - [x] `@SubTypes` (Polymorphic models)
- [x] Task: Test Model metadata & sorting
    - [x] Field sorting (ID first, DeleteFlag last)
    - [x] Partitioning metadata

## Phase 2: DataManagerImpl Tests
This phase focuses on the bridge between commands and their executions.

- [x] Task: Test Command Routing
    - [x] Verify `execCommand` routes to the correct `Execution` implementation
    - [x] Test behavior when an executor is missing
- [x] Task: Test Context Access
    - [x] Verify `getModel()` and `getModelContext()` return expected data

## Phase 3: ModelServiceImpl Tests
This phase covers the lifecycle management of models and data managers.

- [x] Task: Test Model Registration
    - [x] Register/Unregister by Model name
    - [x] Register/Unregister by Class
    - [x] Duplicate registration behavior
- [x] Task: Test Model Updates & DDL
    - [x] `update` triggers DDL plan generation and execution
    - [x] `dryRun` mode verification
    - [x] `delete` triggers table drop
- [x] Task: Test DataManager Lifecycle
    - [x] `createDataManager` initialization
    - [x] `getDataManager` with permission handling
- [x] Task: Test Interceptors & Events
    - [x] Verify event listeners are notified on update/delete
    - [x] Verify interceptors are correctly passed to model context

## Phase 4: Verification & Final Polish
- [x] Task: Full Suite Execution
    - [x] Run all `core` module tests
- [x] Task: Coverage Check
    - [x] Verify high coverage for the three classes
