# Specification: Optional Condition Operators Documentation

## Overview
This track involves updating the condition operators documentation in both `README.md` and `README_CN.md`. The goal is to document the "Optional" variants of condition operators (e.g., `eqOptional`, `likeOptional`), which allow the framework to automatically ignore conditions if the provided value is null or empty.

## Functional Requirements

### 1. Document Optional Operators
- Update the "Condition Operators" section to include `xxOptional` variants for all major operators.
- **Operators to include:**
    - Comparison: `eqOptional`, `neOptional`, `gtOptional`, `gteOptional`, `ltOptional`, `lteOptional`
    - String: `likeOptional`, `startsWithOptional`, `endsWithOptional`, `containsOptional`
    - Collection: `inOptional`, `notInOptional`, `eqOrInOptional`

### 2. Explanation of "Optional" Behavior
- Provide a clear explanation that `xxOptional` operators will automatically exclude the condition from the final SQL if the input value is `null`, empty string, or an empty collection.
- Provide a concise code example demonstrating the difference between `eq` and `eqOptional`.

## Non-Functional Requirements
- **Consistency:** Maintain the same level of detail and formatting as existing operator documentation.
- **Bilingual:** Apply changes to both `README.md` (English) and `README_CN.md` (Chinese).

## Acceptance Criteria
- [ ] Both README files contain documentation for all specified `xxOptional` operators.
- [ ] The behavior of optional operators is clearly explained.
- [ ] A comparison example (Standard vs Optional) is included.
