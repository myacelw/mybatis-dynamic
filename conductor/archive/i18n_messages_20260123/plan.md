# Implementation Plan: Internationalization of Code Messages (CN to EN)

This plan outlines the steps to replace all Chinese string literals in assertions, exceptions, and logs with standard technical English across the `mybatis-dynamic` project.

## Phase 1: Preparation and Discovery
- [x] Task: Identify all Chinese string literals in the codebase
    - [x] Search for non-ASCII characters in `core/src/main/java`
    - [x] Search for non-ASCII characters in `spring/src/main/java`
    - [x] Search for non-ASCII characters in `draw/src/main/java`
    - [x] Search for non-ASCII characters in `sample/src/main/java`
- [x] Task: Create a translation map for common messages to ensure consistency
- [~] Task: Conductor - User Manual Verification 'Phase 1: Preparation and Discovery' (Protocol in workflow.md)

## Phase 2: Refactor `core` module
- [x] Task: Write Tests: Create/Update tests in `core` to verify existing behavior before string replacement
- [x] Task: Implement: Replace Chinese strings in `core` module
    - [x] Replace in `Assert` statements
    - [x] Replace in `throw` statements
    - [x] Replace in `log` statements
- [x] Task: Verify: Run `mvn clean test -pl core` to ensure no regressions
- [x] Task: Conductor - User Manual Verification 'Phase 2: Refactor core module' (Protocol in workflow.md)

## Phase 3: Refactor `spring` module
- [x] Task: Write Tests: Create/Update tests in `spring` to verify existing behavior
- [x] Task: Implement: Replace Chinese strings in `spring` module
    - [x] Replace in `Assert` statements
    - [x] Replace in `throw` statements
    - [x] Replace in `log` statements
- [x] Task: Verify: Run `mvn clean test -pl spring`
- [x] Task: Conductor - User Manual Verification 'Phase 3: Refactor spring module' (Protocol in workflow.md)

## Phase 4: Refactor `draw` and `sample` modules
- [x] Task: Write Tests: Create/Update tests for `draw` and `sample`
- [x] Task: Implement: Replace Chinese strings in `draw` and `sample` modules
    - [x] Replace in `Assert` statements
    - [x] Replace in `throw` statements
    - [x] Replace in `log` statements
- [x] Task: Verify: Run `mvn clean test -pl draw,sample`
- [x] Task: Conductor - User Manual Verification 'Phase 4: Refactor draw and sample modules' (Protocol in workflow.md)

## Phase 5: Final Verification and Cleanup
- [x] Task: Final Scan: Perform a global search for any remaining Chinese characters in `.java` files
- [x] Task: Full Build: Run `mvn clean install` for the entire project
- [x] Task: Conductor - User Manual Verification 'Phase 5: Final Verification and Cleanup' (Protocol in workflow.md)
