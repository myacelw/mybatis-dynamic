# Specification: DDL Migration Strategy & Logging

## Overview
To improve the safety of database schema changes in production environments, this track introduces a "Plan/Execute" model for DDL. Instead of always automatically updating the database at startup, `mybatis-dynamic` will now support logging all generated DDL statements to dedicated SQL files and a "Dry Run" mode to preview changes without applying them.

## Functional Requirements

### 1. DDL Logging
- **Trigger**: When the framework identifies a need to create or alter a table (and `update-model` is enabled or in `dry-run` mode).
- **File Creation**: A SQL log file is created **only if** DDL statements are generated. This avoids cluttering the system with empty files.
- **Filename Pattern**: `ddl_{TIMESTAMP}.sql` (e.g., `ddl_20260117_153000.sql`).
- **Storage Location**: Files are stored in a `./ddl-logs/` directory relative to the project root by default.
- **Content**: The file should contain valid SQL statements formatted for the active database dialect.

### 2. Dry Run Mode
- **Configuration**: Enabled via `mybatis-dynamic.ddl.dry-run: true` in `application.yml`.
- **Behavior**: 
    - The framework performs the full model-to-table comparison.
    - Generated DDL is printed to the console (STDOUT).
    - Generated DDL is written to the timestamped log file in `./ddl-logs/`.
    - **CRITICAL**: No `CREATE TABLE` or `ALTER TABLE` statements are executed against the database.

### 3. Workflow Integration
- **Development**: Enable `update-model: true` and `dry-run: false` (default) to keep the DB in sync and collect DDL logs.
- **Production**: Set `update-model: false`. The operator can take the `.sql` files generated during development/testing and execute them manually using their preferred DBA tools before deploying the application.

## Non-Functional Requirements
- **Dialect Awareness**: The logged SQL must strictly adhere to the target database dialect (MySQL, Postgres, etc.).
- **Error Handling**: Failure to write to the log directory should log a warning but not necessarily prevent the application from starting (unless in a strict migration mode).

## Acceptance Criteria
- [ ] Setting `mybatis-dynamic.ddl.dry-run: true` successfully prevents any schema changes in the database.
- [ ] SQL statements generated during startup are correctly captured in `./ddl-logs/ddl_*.sql`.
- [ ] No log file is created if the database schema already matches the model.
- [ ] Console output clearly indicates when "Dry Run" mode is active.

## Out of Scope
- Automatic cleanup of old log files (rotation).
- Full integration with Flyway/Liquibase (though this paves the way for it).
- Support for rolling back DDL.
