# Plan: Refactor Database Dialect Metadata Logic

## Phase 1: Interface Expansion and Infrastructure [checkpoint: aaeeeff]
Define the new contracts and set up the caching mechanism to ensure performance and consistency.

- [x] Task: Add new metadata methods to `DataBaseMetaDataHelper` interface (`getIdentifierQuoteString`, `isIdentifierReserved`, `wrapIdentifier`, `unwrapIdentifier`, `getIdentifierInMeta`) [0adad95]
- [x] Task: Implement a thread-safe caching structure in `DataBaseMetaDataHelperImpl` to store `DatabaseMetaData` properties (e.g., quote string, keyword sets, case-sensitivity flags) keyed by the database connection URL or DataSource identity [37b3c5d]
- [ ] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)

## Phase 2: Implement Dynamic Metadata Logic [checkpoint: 64c532b]
Implement the core logic for interacting with JDBC `DatabaseMetaData` and managing keywords.

- [x] Task: Implement `getIdentifierQuoteString` and `getIdentifierInMeta` in `DataBaseMetaDataHelperImpl` using JDBC metadata flags (`storesUpperCaseIdentifiers`, etc.) [64a023f]
- [x] Task: Implement `isIdentifierReserved` by merging a static standard SQL keyword set with dynamic results from `getSQLKeywords()` and JDBC function lists [337b5a6]
- [x] Task: Implement `wrapIdentifier` and `unwrapIdentifier` logic using the dynamically retrieved quote string and reserved keyword check [43215ce]
- [ ] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)

## Phase 3: Refactor Dialect and Update Callers
Remove the legacy static logic and migrate all components to the new metadata helper.

- [x] Task: Remove `wrapper`, `unWrapper`, `getEscapeCharacter`, `isTableNameUpperCase`, and `KEYWORD` from `DataBaseDialect` interface and `AbstractDataBaseDialect` [dbd72b7]
- [x] Task: Update all callers of the removed methods (e.g., in `TableManager`, `AbstractDataBaseDialect` subclasses, and internal DDL generators) to use the corresponding methods in `DataBaseMetaDataHelper` [2f21943]
- [ ] Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)

## Phase 4: Testing and Verification
Ensure the refactoring maintains compatibility across supported databases and correctly handles edge cases.

- [ ] Task: Create unit tests for `DataBaseMetaDataHelperImpl` using a mock or H2 connection to verify dynamic quoting and casing behavior.
- [ ] Task: Run existing integration tests in the `sample` and `core` modules to ensure no regressions in schema generation or CRUD operations.
- [ ] Task: Conductor - User Manual Verification 'Phase 4' (Protocol in workflow.md)
