# Implementation Plan: Refine Database Operation Interfaces

## Phase 1: Core Interface Standardization
- [x] Task: Refactor `DataManager` Interface
    - [x] Update `countRecursive` return type from `long` to `int`
    - [x] Standardize `aggQueryChain` naming (Recommendation: `aggQuery`)
    - [x] Ensure consistent naming for all `updateOnlyNonNull` variants
- [x] Task: Update `DataManagerImpl` and Execution Engine
    - [x] Update `DataManagerImpl` to match interface changes
    - [x] Update `CountRecursiveExecution` to return `int`
    - [x] Ensure all internal commands and executions use the unified `int` type for counts
- [x] Task: Write failing tests for `DataManager` changes
- [x] Task: Conductor - User Manual Verification 'Core Interface Standardization' (Protocol in workflow.md)

## Phase 2: Spring Module Interface Standardization
- [x] Task: Refactor `BaseDao` Interface
    - [x] Rename `onlyUpdateNonNull` to `updateOnlyNonNull`
    - [x] Update `count` methods to return `int` instead of `long`
    - [x] Standardize `agg` to `aggQuery` (or consistent choice from Phase 1)
- [x] Task: Refactor `BaseService` and `BaseServiceImpl`
    - [x] Rename `onlyUpdateNonNull` to `updateOnlyNonNull`
    - [x] Update `count` methods to return `int` instead of `long`
    - [x] Update `BaseServiceImpl` to match interface changes
- [x] Task: Write failing tests for `BaseDao`/`BaseService` changes
- [x] Task: Conductor - User Manual Verification 'Spring Module Interface Standardization' (Protocol in workflow.md)

## Phase 3: Documentation & Javadoc Enhancement
- [x] Task: Comprehensive Javadoc Update
    - [x] Add/Update Javadoc for all public methods in `DataManager`
    - [x] Add/Update Javadoc for all public methods in `BaseDao`
    - [x] Add/Update Javadoc for all public methods in `BaseService`
- [x] Task: Update User Documentation
    - [x] Update `README.md` with refined API examples
    - [x] Ensure all "Best Practice" code snippets in documentation are current
- [x] Task: Conductor - User Manual Verification 'Documentation & Javadoc Enhancement' (Protocol in workflow.md)

## Phase 4: Final Verification & Cleanup [checkpoint: de704a3]
- [x] Task: Project-wide Verification [e2fb70f]
    - [x] Run `mvn clean install` to ensure all modules build and tests pass
    - [x] Verify that `sample` module still functions correctly with refined APIs
- [x] Task: Conductor - User Manual Verification 'Final Verification' [de704a3]
