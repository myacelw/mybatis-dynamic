# Implementation Plan: Query & DataManager Documentation Overhaul

## Phase 1: Analysis & Reorganization [checkpoint: 151c4d2]
- [x] Task: Audit DataManager & ConditionBuilder APIs
    - [x] List all `DataManager` methods (CRUD, Batch, Tree, etc.) from `core`.
    - [x] List all condition operators in `ConditionBuilder` (simple, logical, exists, custom).
- [x] Task: Apply Role-Based Structure
    - [x] Restructure `README.md` into Beginner, Power User, and Architect sections.
    - [x] Restructure `README_CN.md` into Beginner, Power User, and Architect sections.
- [x] Task: Conductor - User Manual Verification 'Analysis & Reorganization' (Protocol in workflow.md)

## Phase 2: Power User Documentation (Advanced Queries) [checkpoint: TBD]
- [ ] Task: Document Auto-Joins & Join Configuration
    - [ ] Explain automatic join triggering via field selection.
    - [ ] Document `Join` type configuration and additional conditions.
- [ ] Task: Comprehensive Query Condition Guide
    - [ ] Document simple conditions (eq, gt, like, etc.).
    - [ ] Document logical operators (and, or, not) with precedence explanation.
    - [ ] Document `Exists` and `Custom` conditions.
- [ ] Task: Conductor - User Manual Verification 'Power User Documentation' (Protocol in workflow.md)

## Phase 3: Power User Documentation (DataManager & Batch) [checkpoint: TBD]
- [ ] Task: Summarize DataManager API
    - [ ] Provide method signatures and brief descriptions for all key `DataManager` methods.
- [ ] Task: Document Batch & Hierarchical Operations
    - [ ] Add examples for `batchInsert`, `batchUpdate`, and `batchUpdateByCondition`.
    - [ ] Add examples for recursive tree queries.
- [ ] Task: Conductor - User Manual Verification 'Power User Documentation (DataManager & Batch)' (Protocol in workflow.md)

## Phase 4: Architect Documentation & Final Polish [checkpoint: TBD]
- [ ] Task: Refine Expert Features
    - [ ] Ensure Interceptors, Fillers, and Multi-tenancy are correctly categorized under "Architect".
- [ ] Task: Final Review & Consistency Check
    - [ ] Verify bilingual consistency between English and Chinese versions.
    - [ ] Verify all code snippets against the `sample` project or core tests.
- [ ] Task: Conductor - User Manual Verification 'Architect Documentation & Final Polish' (Protocol in workflow.md)
