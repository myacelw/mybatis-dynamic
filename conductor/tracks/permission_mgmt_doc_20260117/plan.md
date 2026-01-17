# Implementation Plan: Permission Management Documentation Overhaul

## Phase 1: API Audit & Mapping [checkpoint: TBD]
- [x] Task: Audit Permission-related Interfaces
    - [x] List all methods in `Permission.java` and `CurrentUserHolder.java` from `core` and `spring`.
    - [x] Verify usage of these interfaces in `DataManager` and `BaseDao`.
- [x] Task: Update Category Mapping
    - [x] Ensure "Permission Management" remains under the "Expert Features" (Architect) section.
- [x] Task: Conductor - User Manual Verification 'API Audit & Mapping' (Protocol in workflow.md)

## Phase 2: Content Generation (English) [checkpoint: TBD]
- [ ] Task: Draft "Permission Management" Section
    - [ ] Define Column and Row permissions.
    - [ ] Provide `Permission` interface implementation example.
    - [ ] Provide `CurrentUserHolder` integration example.
- [ ] Task: Replace "Multi-Tenancy" in `README.md`
    - [ ] Integrate the new content into the English README.
- [ ] Task: Conductor - User Manual Verification 'Content Generation (English)' (Protocol in workflow.md)

## Phase 3: Content Generation (Chinese) [checkpoint: TBD]
- [ ] Task: Translate & Localize "Permission Management"
    - [ ] Translate the content from Phase 2 into Chinese.
- [ ] Task: Replace "Multi-Tenancy" in `README_CN.md`
    - [ ] Integrate the new content into the Chinese README.
- [ ] Task: Conductor - User Manual Verification 'Content Generation (Chinese)' (Protocol in workflow.md)

## Phase 4: Final Review & Polish [checkpoint: TBD]
- [ ] Task: Verify Code Snippets & Links
    - [ ] Ensure all code examples are accurate and links are functional.
- [ ] Task: Final Bilingual Consistency Check
    - [ ] Verify that English and Chinese versions are perfectly synced in structure and meaning.
- [ ] Task: Conductor - User Manual Verification 'Final Review & Polish' (Protocol in workflow.md)
