# Plan: Condition Inheritance of Cloneable Interface

## Phase 1: Base and Leaf Conditions
- [x] Task: TDD - Support cloning for `Condition` interface and `SimpleCondition` [de703da]
    - [ ] Sub-task: Create test `ConditionCloneTest.java` to verify `Condition` is `Cloneable` and `SimpleCondition` can be cloned.
    - [ ] Sub-task: Modify `Condition.java` to extend `Cloneable`.
    - [ ] Sub-task: Modify `SimpleCondition.java` to override `clone()`.
    - [ ] Sub-task: Verify tests pass.
- [ ] Task: TDD - Support cloning for simple leaf conditions
    - [ ] Sub-task: Add tests for `CustomCondition`, `ExistsCondition`, and `SearchCondition` to `ConditionCloneTest.java`.
    - [ ] Sub-task: Implement `clone()` in `CustomCondition`.
    - [ ] Sub-task: Implement `clone()` in `ExistsCondition`.
    - [ ] Sub-task: Implement `clone()` in `SearchCondition`.
    - [ ] Sub-task: Verify tests pass.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Base and Leaf Conditions' (Protocol in workflow.md)

## Phase 2: Recursive Conditions (Deep Copy)
- [ ] Task: TDD - Support deep cloning for `NotCondition`
    - [ ] Sub-task: Add test to `ConditionCloneTest.java` specifically checking deep copy of `NotCondition`.
    - [ ] Sub-task: Implement `clone()` in `NotCondition` to clone its inner condition.
    - [ ] Sub-task: Verify tests pass.
- [ ] Task: TDD - Support deep cloning for `GroupCondition`
    - [ ] Sub-task: Add test to `ConditionCloneTest.java` specifically checking deep copy of `GroupCondition` (nested lists).
    - [ ] Sub-task: Implement `clone()` in `GroupCondition` to iterate and clone children.
    - [ ] Sub-task: Verify tests pass.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Recursive Conditions (Deep Copy)' (Protocol in workflow.md)
