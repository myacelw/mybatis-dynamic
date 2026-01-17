# Specification: Dynamic REST API Controller

## Overview
This track aims to implement a "zero-code" REST API interface for `mybatis-dynamic`. By introducing a generic `DynamicModelController`, users will be able to perform CRUD operations on any registered model (whether defined via Java classes or dynamic `Model` objects) without writing manual Controller code.

## Functional Requirements

### 1. Base Path & Mapping
- **Base URL**: `/api/dynamic/{modelName}`
- The `{modelName}` path variable corresponds to the unique name of the model in the `ModelService`.

### 2. Supported Operations
- **List/Search** (`GET`):
    - Retrieves a list of records.
    - **Filtering**: Supports simple `AND` logic via query parameters (e.g., `?age=gt:18&name=zhang`). 
    - **Pagination**: Supports `page` (current page, 1-based) and `size` (page size).
    - **Sorting**: Supports sorting parameters (e.g., `?sort=age,desc&sort=name,asc`).
    - **Limit**: Supports a maximum row limit.
- **Get by ID** (`GET`):
    - `GET /api/dynamic/{modelName}/{id}`
    - Retrieves a single record by its primary key.
- **Create** (`POST`):
    - `POST /api/dynamic/{modelName}`
    - Accepts a JSON body and inserts the record. Returns the generated ID.
- **Update** (`PUT`/`PATCH`):
    - `PUT /api/dynamic/{modelName}`: Full update (ID must be in body).
    - Support for partial updates (updating only provided fields).
- **Delete** (`DELETE`):
    - `DELETE /api/dynamic/{modelName}/{id}`
    - Performs a deletion (logical or physical based on model configuration).
- **Batch Operations**:
    - Support for batch insertion and updates via specific endpoints if necessary, or by accepting lists in standard endpoints.

### 3. Error Handling
- Return `404 Not Found` if the `{modelName}` does not exist.
- Return `400 Bad Request` for invalid filtering syntax or validation errors.
- Standardized JSON error responses.

## Non-Functional Requirements
- **Security**: Must respect `Permission` (Row and Column rights) provided by `CurrentUserHolder` if implemented.
- **Performance**: Ensure that dynamic query building does not introduce significant overhead.
- **Extensibility**: Allow users to disable this feature or override specific model endpoints if needed.

## Acceptance Criteria
- [ ] Any model registered in `ModelService` is accessible via `/api/dynamic/{modelName}`.
- [ ] `GET` requests with filtering, sorting, and pagination return expected results.
- [ ] `POST`, `PUT`, and `DELETE` operations correctly modify data in the database.
- [ ] Field-level and row-level permissions are applied to the REST results.
- [ ] Automatic DDL remains functional (table is created/updated before the first API call if `update-model` is enabled).

## Out of Scope
- Support for complex nested `OR`/`NOT` conditions in the URL.
- Custom specialized search endpoints (e.g., `/search` with JSON body).
