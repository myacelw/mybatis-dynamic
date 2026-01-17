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

## Phase 2: Dynamic Controller Implementation
Implement the generic controller in the `spring` module.

- [~] Task: Implement `DynamicModelController` in `io.github.myacelw.mybatis.dynamic.spring.controller`.
    - [ ] Add `@RestController` and `@RequestMapping("/api/dynamic/{modelName}")`.
    - [ ] Implement `GET /` for list/search with pagination and sorting.
    - [ ] Implement `GET /{id}` for retrieval by ID.
    - [ ] Implement `POST /` for creation.
    - [ ] Implement `PUT /` for update.
    - [ ] Implement `DELETE /{id}` for deletion.
- [ ] Task: Integrate `Permission` handling into the controller to ensure row/column security.
- [ ] Task: Add configuration properties to enable/disable the dynamic controller (e.g., `mybatis-dynamic.rest.enabled`).
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Dynamic Controller Implementation' (Protocol in workflow.md)

## Phase 3: Integration and Sample Verification
Verify the functionality using the `sample` module.

- [ ] Task: Enable the dynamic REST feature in the `sample` application.
- [ ] Task: Write integration tests (using `MockMvc`) to verify end-to-end CRUD via the new REST endpoints.
    - [ ] Test listing with filters and pagination.
    - [ ] Test security (ensuring permissions are respected).
- [ ] Task: Update documentation (README.md) to include the new REST API feature.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Integration and Sample Verification' (Protocol in workflow.md)
