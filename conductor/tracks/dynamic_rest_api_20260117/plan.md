# Implementation Plan: Dynamic REST API Controller

This plan outlines the steps to implement a generic REST API controller that enables CRUD operations for any model registered in `mybatis-dynamic` without requiring manual controller code.

## Phase 1: Core Service Support for Generic Operations [checkpoint: 0681a79]
Focus on ensuring `ModelService` or a helper can easily translate URL parameters into `mybatis-dynamic` conditions and query chains.

- [x] Task: Create `RestRequestParser` utility to parse query parameters into `Condition` and `Page`. [621a776]
    - [x] Implement parsing of simple operators (eq, gt, lt, like, etc.) using `field=op:value` syntax.
    - [x] Implement parsing of `page` and `size` parameters.
    - [x] Implement parsing of `sort` parameters (e.g., `field,asc`).
- [x] Task: Write unit tests for `RestRequestParser` with various parameter combinations. [621a776]
- [x] Task: Conductor - User Manual Verification 'Phase 1: Core Service Support' (Protocol in workflow.md) [0681a79]

## Phase 2: Dynamic Controller Implementation [checkpoint: 1bdf780]
Implement the generic controller in the `spring` module.

- [x] Task: Implement `DynamicModelController` in `io.github.myacelw.mybatis.dynamic.spring.controller`. [cb1695c]
    - [x] Add `@RestController` and `@RequestMapping("/api/dynamic/{modelName}")`.
    - [x] Implement `GET /` for list/search with pagination and sorting.
    - [x] Implement `GET /{id}` for retrieval by ID.
    - [x] Implement `POST /` for creation.
    - [x] Implement `PUT /` for update.
    - [x] Implement `DELETE /{id}` for deletion.
- [x] Task: Integrate `Permission` handling into the controller to ensure row/column security. [945c247]
    - [x] Inject `CurrentUserHolder` (optional).
    - [x] Retrieve `Permission` using `currentUserHolder.getCurrentUserPermission(model)`.
    - [x] Use `modelService.createDataManager(model, permission, null)` instead of `getDataManager`.
- [x] Task: Add configuration properties to enable/disable the dynamic controller (e.g., `mybatis-dynamic.rest.enabled`). [Impl in DynamicModelController]
- [x] Task: Conductor - User Manual Verification 'Phase 2: Dynamic Controller Implementation' (Protocol in workflow.md)

## Phase 3: Integration and Sample Verification [checkpoint: d67f456]
Verify the functionality using the `sample` module.

- [x] Task: Enable the dynamic REST feature in the `sample` application. (Enabled by default)
- [x] Task: Write integration tests (using `MockMvc`) to verify end-to-end CRUD via the new REST endpoints.
    - [x] Test listing with filters and pagination.
    - [x] Test security (ensuring permissions are respected).
- [x] Task: Update documentation (README.md) to include the new REST API feature.
- [x] Task: Conductor - User Manual Verification 'Phase 3: Integration and Sample Verification' (Protocol in workflow.md)
