# Specification: Internationalization of Code Messages (CN to EN)

## Overview
The goal of this track is to replace all Chinese string literals used in assertions, exception messages, and log entries with standard technical English across the entire `mybatis-dynamic` project. This will improve codebase accessibility for international developers and align the project with standard open-source practices.

## Scope
- **Modules**: `core`, `spring`, `draw`, `sample`
- **Targets**:
    - `Assert` messages (e.g., `Assert.notNull(obj, "消息")`)
    - Exception messages (e.g., `throw new RuntimeException("消息")`)
    - Log messages (e.g., `log.info("消息")`, `log.error("错误", e)`)

## Functional Requirements
1. **Search and Identify**: Systematically scan the source code in all specified modules for Chinese characters within string literals associated with assertions, exceptions, and logging.
2. **Translate and Replace**: Replace Chinese text with accurate, concise English equivalents using "Direct Replacement" as the implementation strategy.
3. **Standard Phrasing**: Use standard technical English terminology (e.g., "Parameter cannot be null", "Record not found", "Operation failed").

## Non-Functional Requirements
- **Consistency**: Maintain consistent phrasing for recurring error types across different modules.
- **Code Quality**: Ensure that the replacement does not break any string formatting (e.g., `{}` placeholders in SLF4J logs).

## Acceptance Criteria
- [ ] All Chinese string literals in `Assert`, `throw`, and `log` statements are replaced with English.
- [ ] No Chinese characters are found in the source code of the specified modules (excluding comments/Javadoc).
- [ ] The project builds successfully (`mvn clean install`).
- [ ] All existing tests pass.

## Out of Scope
- Translation of Javadoc or code comments.
- Modification of dedicated Chinese documentation files (e.g., `README_CN.md`).
- Implementation of an i18n/Resource Bundle system (Direct Replacement only).
