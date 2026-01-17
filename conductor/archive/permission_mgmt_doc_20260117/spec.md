# Specification: Permission Management Documentation Overhaul

## Overview
This track involves updating the project documentation (`README.md` and `README_CN.md`) to replace the "Multi-Tenancy" section with a comprehensive "Permission Management" section. The new section will detail both Column (Field) and Row (Data) level permissions, explain how they apply to various database operations, and provide guidance on implementing the `Permission` and `CurrentUserHolder` interfaces.

## Functional Requirements

### 1. Section Renaming & Reorganization
- Rename the existing "Multi-Tenancy" section to **"Permission Management"**.
- Position Multi-Tenancy as a primary use case for Row Permissions.

### 2. Column Permissions (Field Level)
- Explain how to control access to specific model fields.
- Document that these permissions can restrict which fields are returned in queries and which can be updated.

### 3. Row Permissions (Data Level)
- Explain how to filter data based on user context (e.g., `tenantId`, `ownerId`).
- Document the application of row permissions across:
    - **Select:** Filtering result sets.
    - **Update/Delete:** Restricting operations to authorized rows.

### 4. Technical Implementation Guidance
- **`Permission` Interface:**
    - Document the key methods: `getFieldRights()` and `getDataRights()`.
    - Provide an implementation example.
- **`CurrentUserHolder` Interface:**
    - Explain its role in bridging the framework with security contexts (e.g., Spring Security).
    - Show how to implement and register it as a bean.

## Non-Functional Requirements
- **Consistency:** Ensure terms like "Column Permissions" and "Row Permissions" are used consistently.
- **Bilingual:** All changes must be applied to both English (`README.md`) and Chinese (`README_CN.md`) documentation.

## Acceptance Criteria
- [ ] "Multi-Tenancy" section is replaced by "Permission Management" in both READMEs.
- [ ] Column and Row permissions are clearly defined with examples.
- [ ] `Permission` and `CurrentUserHolder` interfaces are documented with code snippets.
- [ ] Multi-Tenancy is correctly described as a subset of Row Permissions.
