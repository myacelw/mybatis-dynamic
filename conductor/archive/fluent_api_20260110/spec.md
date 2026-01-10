# Specification: Fluent API for ConditionBuilder

## 1. Overview
The goal is to redesign `ConditionBuilder` to provide a more natural, fluent API for constructing complex SQL `WHERE` clauses. The new API should support explicit `and`, `or`, and `not` connectors as fields, allowing for intuitive chaining like `.and().bracket(...)` or `.or().eq(...)`. It must also correctly handle SQL operator precedence (AND binds tighter than OR).

## 2. User Story
As a developer using `mybatis-dynamic`, I want to write query conditions in a way that mimics natural language and SQL structure, so that my code is easier to read and maintain.

**Example:**
*   **SQL:** `a = 1 AND (b > 2 OR c < 3) AND d = 4`
*   **New API:** `cb.eq("a", 1).and().bracket(cb2 -> cb2.gt("b", 2).or().lt("c", 3)).eq("d", 4)`
*   **Simplified:** `cb.eq("a", 1).bracket(cb2 -> cb2.gt("b", 2).or().lt("c", 3)).eq("d", 4)` (Default is AND)

## 3. Key Features

### 3.1 Fluent Connectors
*   Introduce public final fields `and`, `or`, and `not` in `ConditionBuilder`.
*   These fields return specialized Connector classes (`AndConnector`, `OrConnector`, `NotConnector`) that maintain the context and apply the appropriate logic.

### 3.2 Logical Precedence
*   **AND Priority:** Ensure that `AND` operations bind tighter than `OR`.
*   **Grouping:** Automatically group conditions when switching between logical operators to preserve correctness (e.g., `A OR B AND C` -> `A OR (B AND C)` vs `(A OR B) AND C`).

### 3.3 BaseBuilder Abstraction
*   Extract common condition methods (eq, ne, gt, lt, like, in, etc.) into an abstract `BaseBuilder` class.
*   `ConditionBuilder` and all Connector classes must extend `BaseBuilder` to share implementation and ensure API consistency.

### 3.4 Nested Conditions (Brackets)
*   Support a `bracket(Consumer<ConditionBuilder>)` method to create explicit parenthesized groups.
*   This method should be available on the main builder and all connectors.

## 4. Technical Design

### 4.1 Class Structure
*   **`BaseBuilder<R>`**: Abstract base class containing all condition generation methods. Returns generic type `R` (the builder type) for method chaining.
*   **`ConditionBuilder`**: Main entry point. Extends `BaseBuilder<ConditionBuilder>`. Manages the root `GroupCondition`.
*   **`ConditionBuilder.Connector`**: Abstract inner class extending `BaseBuilder`.
*   **`ConditionBuilder.AndConnector`**: Implements AND logic.
*   **`ConditionBuilder.OrConnector`**: Implements OR logic.
*   **`ConditionBuilder.NotConnector`**: Implements NOT logic.

### 4.2 Logic Handling
*   The `add(Condition, Logic)` method in `ConditionBuilder` is the core engine.
*   It analyzes the current root logic and the incoming logic to decide whether to append, group, or wrap conditions.

## 5. Testing Requirements
*   **Unit Tests:** Verify all condition types (simple, nested, custom).
*   **Logic Verification:** Test complex combinations of AND/OR to ensure generated SQL structure is correct.
*   **Backward Compatibility:** Ensure existing simple usages still work (though the internal implementation has changed).
