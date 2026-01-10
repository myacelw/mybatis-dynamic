# Plan: Fluent API for ConditionBuilder

## Phase 1: Implementation (Completed)
This phase focuses on the core refactoring and implementation of the new API structure.

- [x] Task: Refactor ConditionBuilder
    - [x] Create `BaseBuilder` abstract class.
    - [x] Implement `AndConnector`, `OrConnector`, `NotConnector`.
    - [x] Update `ConditionBuilder` to extend `BaseBuilder` and use connectors.
    - [x] Implement logic precedence handling in `add()` method.

## Phase 2: Verification and Testing (Completed)
This phase ensures the correctness of the implementation through rigorous testing.

- [x] Task: Verify Logic Precedence
    - [x] Create test case: `A AND B OR C` -> `A AND B` then `OR C`.
    - [x] Create test case: `A OR B AND C` -> `A OR` then `(B AND C)`.
    - [x] Create test case: `A AND (B OR C)`.
    - [x] Verify generated SQL string or GroupCondition structure.

- [x] Task: Unit Tests for Base Methods
    - [x] Test `eq`, `ne`, `gt`, `lt`, `ge`, `le`.
    - [x] Test `like`, `contains`, `startsWith`, `endsWith`.
    - [x] Test `in`, `notIn`, `isNull`, `isNotNull`.
    - [x] Test `exists` and `not exists`.

- [x] Task: Unit Tests for Fluent API
    - [x] Test chaining: `.and().eq(...)`.
    - [x] Test chaining: `.or().eq(...)`.
    - [x] Test chaining: `.not().eq(...)`.
    - [x] Test nested brackets: `.bracket(...)`.

## Phase 3: Documentation
This phase focuses on updating project documentation to reflect the new API.

- [x] Task: Update README
- [x] Task: JavaDoc