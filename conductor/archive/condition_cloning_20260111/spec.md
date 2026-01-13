# Specification: Condition Inheritance of Cloneable Interface

## Overview
Enable cloning support for the `Condition` hierarchy to allow developers to create independent copies of query conditions. This is particularly useful for building dynamic queries where a base condition might be reused and modified in different contexts.

## Functional Requirements
- `io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition` must implement the `java.lang.Cloneable` interface.
- All subclasses of `Condition` (including but not limited to `CustomCondition`, `ExistsCondition`, `GroupCondition`, `NotCondition`, `SearchCondition`, `SimpleCondition`) must override the `clone()` method.
- The `clone()` implementation must perform a **Deep Copy**. 
    - Specifically, `GroupCondition` must recursively clone its collection of nested conditions.
- The `clone()` method signature should return the specific subtype (covariant return type) and be `public`.

## Non-Functional Requirements
- Maintain code consistency with existing patterns in the `core` module.
- Ensure no `CloneNotSupportedException` is thrown during runtime for these classes.

## Acceptance Criteria
- [ ] `Condition` implements `Cloneable`.
- [ ] All subclasses provide a `public` override of `clone()`.
- [ ] Unit tests verify that a cloned `GroupCondition` contains cloned instances of its child conditions (deep copy check).
- [ ] Modifying a cloned condition tree does not affect the original tree.

## Out of Scope
- Cloning other metadata classes not related to the `Condition` hierarchy.
