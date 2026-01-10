# Plan: Fluent API for ConditionBuilder

## Phase 1: Implementation (Completed)
This phase focuses on the core refactoring and implementation of the new API structure.

- [x] Task: Refactor ConditionBuilder
    - [x] Create `BaseBuilder` abstract class.
    - [x] Implement `AndConnector`, `OrConnector`, `NotConnector`.
    - [x] Update `ConditionBuilder` to extend `BaseBuilder` and use connectors.
    - [x] Implement logic precedence handling in `add()` method.

## Phase 2: Verification and Testing
This phase ensures the correctness of the implementation through rigorous testing.

- [ ] Task: Verify Logic Precedence
    - [ ] Create test case: `A AND B OR C` -> `A AND B` then `OR C`.
    - [ ] Create test case: `A OR B AND C` -> `A OR` then `(B AND C)`.
    - [ ] Create test case: `A AND (B OR C)`.
    - [ ] Verify generated SQL string or GroupCondition structure.

- [ ] Task: Unit Tests for Base Methods
    - [ ] Test `eq`, `ne`, `gt`, `lt`, `ge`, `le`.
    - [ ] Test `like`, `contains`, `startsWith`, `endsWith`.
    - [ ] Test `in`, `notIn`, `isNull`, `isNotNull`.
    - [ ] Test `exists` and `not exists`.

- [ ] Task: Unit Tests for Fluent API
    - [ ] Test chaining: `.and().eq(...)`.
    - [ ] Test chaining: `.or().eq(...)`.
    - [ ] Test chaining: `.not().eq(...)`.
    - [ ] Test nested brackets: `.bracket(...)`.

## Phase 3: Documentation
This phase focuses on updating project documentation to reflect the new API.

- [ ] Task: Update README
    - [ ] Add "Fluent API" section to `README.md`.
    - [ ] Provide examples of old vs. new syntax.
    - [ ] Document the behavior of `and`, `or`, and `bracket`.

- [ ] Task: JavaDoc
    - [ ] Add JavaDoc to `ConditionBuilder` fields and methods.
    - [ ] Add JavaDoc to `BaseBuilder` methods.
