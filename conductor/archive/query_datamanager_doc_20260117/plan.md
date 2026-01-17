# Implementation Plan: Query & DataManager Documentation Overhaul

## Phase 1: Analysis & Reorganization [checkpoint: 151c4d2]
- [x] Task: Audit DataManager & ConditionBuilder APIs
    - [x] List all `DataManager` methods (CRUD, Batch, Tree, etc.) from `core`.
    - [x] List all condition operators in `ConditionBuilder` (simple, logical, exists, custom).
- [x] Task: Apply Role-Based Structure
    - [x] Restructure `README.md` into Beginner, Power User, and Architect sections.
    - [x] Restructure `README_CN.md` into Beginner, Power User, and Architect sections.
- [x] Task: Conductor - User Manual Verification 'Analysis & Reorganization' (Protocol in workflow.md)

## Phase 2: Power User Documentation (Advanced Queries) [checkpoint: 29f2311]
- [x] Task: Document Auto-Joins & Join Configuration
    - [x] Explain automatic join triggering via field selection.
    - [x] Document `Join` type configuration and additional conditions.
- [x] Task: Comprehensive Query Condition Guide
    - [x] Document simple conditions (eq, gt, like, etc.).
    - [x] Document logical operators (and, or, not) with precedence explanation.
    - [x] Document `Exists` and `Custom` conditions.
- [x] Task: Conductor - User Manual Verification 'Power User Documentation' (Protocol in workflow.md)

## Phase 3: Power User Documentation (DataManager & Batch) [checkpoint: 255966f]
- [x] Task: Summarize DataManager API
    - [x] Provide method signatures and brief descriptions for all key `DataManager` methods.
- [x] Task: Document Batch & Hierarchical Operations
    - [x] Add examples for `batchInsert`, `batchUpdate`, and `batchUpdateByCondition`.
    - [x] Add examples for recursive tree queries.
- [x] Task: Conductor - User Manual Verification 'Power User Documentation (DataManager & Batch)' (Protocol in workflow.md)

## Phase 4: Architect Documentation & Final Polish [checkpoint: TBD]
- [x] Task: Refine Expert Features
    - [x] Ensure Interceptors, Fillers, and Multi-tenancy are correctly categorized under "Architect".
- [x] Task: Final Review & Consistency Check
    - [x] Verify bilingual consistency between English and Chinese versions.
    - [x] Verify all code snippets against the `sample` project or core tests.
- [x] Task: Conductor - User Manual Verification 'Architect Documentation & Final Polish' (Protocol in workflow.md)
