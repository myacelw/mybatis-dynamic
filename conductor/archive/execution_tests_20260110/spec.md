# Specification: Integration Tests for Execution Implementations

## 1. Overview
This track aims to implement comprehensive integration tests for all `io.github.myacelw.mybatis.dynamic.core.service.execution.Execution` implementation classes. The goal is to ensure that each Command execution correctly interacts with a real database (H2) and produces the expected results.

## 2. Scope
The following is within the scope of this track:
*   Writing integration tests for all classes implementing the `Execution` interface in the `io.github.myacelw.mybatis.dynamic.core.service.execution` package.
*   Tests will use an in-memory H2 database.
*   Tests will NOT use mocking for core components like `SqlSession`, `MybatisHelper`, or `DataManager`. Instead, they will use real instances.
*   Focusing on "Happy Path" scenarios to ensure correct end-to-end functionality.

## 3. Functional Requirements
*   **Test Coverage:** Each `Execution` implementation must have a corresponding integration test.
*   **Database Verification:** Tests must verify the actual state of the database after execution (e.g., querying to check if a record was inserted or updated).
*   **Real Components:** Use real `ModelService`, `DataManager`, and `SqlSession` initialized with an H2 datasource.

## 4. Non-Functional Requirements
*   **Code Style:** Tests should follow the existing project conventions.
*   **Performance:** Tests should be reasonably fast using in-memory H2.

## 5. Acceptance Criteria
*   All new integration tests pass successfully against an H2 database.
*   Each `Execution` implementation has at least one test case covering its main functionality with real DB interaction.