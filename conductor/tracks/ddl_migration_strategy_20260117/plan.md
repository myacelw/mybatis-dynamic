# Implementation Plan: DDL Migration Strategy & Logging

This plan outlines the steps to introduce a "Dry Run" mode and DDL logging to improve the safety of database schema migrations.

## Phase 1: Configuration & Core DDL Interception
Update the configuration and modify the core DDL generation logic to support interception.

- [x] Task: Define new configuration properties in `DynamicModelProperties`. [44f1cb0]
    - [x] Add `mybatis-dynamic.ddl.dry-run` (default: false).
    - [x] Add `mybatis-dynamic.ddl.log-path` (default: "./ddl-logs").
- [x] Task: Refactor `ModelToTableConverter` to return generated DDL instead of executing it directly. [8257355]
    - [x] Introduce a `DDLPlan` object to hold the list of SQL statements.
    - [x] Update `ModelService.update()` to handle the execution logic based on the `dry-run` flag.
- [x] Task: Write unit tests to verify that DDL is generated but not executed when `dry-run` is active. [8257355]
- [x] Task: Conductor - User Manual Verification 'Phase 1: Configuration & Core Interception' (Protocol in workflow.md) [ab54473]

## Phase 2: DDL Logging Implementation
Implement the file-based logging mechanism for generated SQL.

- [ ] Task: Create a `DDLFileLogger` utility.
    - [ ] Implement timestamped filename generation (`ddl_yyyyMMdd_HHmmss.sql`).
    - [ ] Implement logic to ensure the directory exists and the file is created **only** if DDL is non-empty.
- [ ] Task: Integrate `DDLFileLogger` into the `ModelService` startup sequence.
- [ ] Task: Implement console output (STDOUT) for generated DDL when in `dry-run` mode.
- [ ] Task: Write unit tests for `DDLFileLogger` to verify file creation and content accuracy.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Logging Implementation' (Protocol in workflow.md)

## Phase 3: Integration & Sample Verification
Verify the end-to-end flow in a Spring environment.

- [ ] Task: Update `DynamicModelAutoConfiguration` to pass the new DDL settings to the core engine.
- [ ] Task: Create an integration test in the `sample` or `spring` module.
    - [ ] Verify that a startup with a new model generates a log file in the configured path.
    - [ ] Verify that `dry-run: true` results in no schema changes in the H2 database.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Integration & Sample Verification' (Protocol in workflow.md)
