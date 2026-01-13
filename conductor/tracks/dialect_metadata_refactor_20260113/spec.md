# Specification: Refactor Database Dialect Metadata Logic

## Overview
This track involves refactoring the database dialect system to move metadata-driven logic (identifier quoting, case sensitivity handling, and keyword wrapping) from static or subclass-defined implementations in `AbstractDataBaseDialect` to a dynamic, metadata-aware implementation in `DataBaseMetaDataHelper`.

## Functional Requirements
1.  **Remove Legacy Methods:** Remove `KEYWORD`, `isTableNameUpperCase`, `getEscapeCharacter`, `wrapper`, and `unWrapper` from the `DataBaseDialect` interface and `AbstractDataBaseDialect`.
2.  **Enhance `DataBaseMetaDataHelper`:** Add new methods to `DataBaseMetaDataHelper` to provide the functionality previously handled by the removed dialect methods:
    *   `getIdentifierQuoteString()`: Returns the quote character (e.g., `"`, `` ` ``, `[`).
    *   `isIdentifierReserved(String identifier)`: Checks if a string is a reserved keyword or requires quoting.
    *   `wrapIdentifier(String identifier)`: Wraps an identifier in escape characters if necessary.
    *   `unwrapIdentifier(String identifier)`: Removes escape characters from an identifier.
    *   `getIdentifierInMeta(String identifier, boolean isQuoted)`: Determines the correct casing for storing/retrieving identifiers from metadata based on database behavior (e.g., `storesUpperCaseIdentifiers`).
3.  **Dynamic Metadata Retrieval:** Use `connection.getMetaData()` to dynamically fetch:
    *   `supportsMixedCaseIdentifiers`, `storesUpperCaseIdentifiers`, `storesLowerCaseIdentifiers`, `storesMixedCaseIdentifiers`
    *   `supportsMixedCaseQuotedIdentifiers`, `storesUpperCaseQuotedIdentifiers`, `storesLowerCaseQuotedIdentifiers`, `storesMixedCaseQuotedIdentifiers`
    *   `getIdentifierQuoteString`
    *   `getSQLKeywords`, `getNumericFunctions`, `getStringFunctions`, `getSystemFunctions`, `getTimeDateFunctions`, `getExtraNameCharacters`
4.  **Keyword Management:** Combine a static list of standard SQL keywords with the dynamic "extra" keywords and functions returned by the JDBC driver.
5.  **Caching Mechanism:** Implement a global application-level cache (mapped by `DataSource` or JDBC connection URL) in `DataBaseMetaDataHelperImpl` to store `DatabaseMetaData` results that do not change during the application's lifetime.
6.  **Update Callers:** Identify and update all locations in the codebase (e.g., `TableManager`, `AbstractDataBaseDialect` subclasses) that previously relied on the removed dialect methods to use the enhanced `DataBaseMetaDataHelper`.

## Non-Functional Requirements
*   **Performance:** Minimize JDBC metadata calls through effective caching.
*   **Maintainability:** Centralize database behavioral logic in the metadata helper rather than scattering it across dialects.

## Acceptance Criteria
*   The `DataBaseDialect` interface no longer contains metadata-related string manipulation methods.
*   The `AbstractDataBaseDialect` no longer defines a static `KEYWORD` set.
*   Identifiers are correctly quoted/unquoted and cased based on the actual connected database's metadata.
*   Unit tests verify that metadata is cased correctly for H2, MySQL, and PostgreSQL (as available in the test suite).
*   The system successfully identifies reserved keywords dynamically.

## Out of Scope
*   Adding support for new database dialects.
*   Refactoring the DDL generation logic (except where it calls the removed methods).
