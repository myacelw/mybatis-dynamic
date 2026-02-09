# 5. PermissionGetter & CurrentUserHolder

This is the security engine. It intercepts **every** database query to inject security constraints.

### 5.1 The Interface: `CurrentUserHolder`
In a Spring application, you implement this interface to bridge your security context (Spring Security/Shiro) with the ORM.

#### `getCurrentUserId()`
*   **Use Case**: The framework automatically fills `@BasicField(fill=FillType.INSERT_USER)` fields using this ID.

#### `getCurrentUserPermission(Model model)`
*   **Use Case**: Return the permissions for the specific model being accessed.
*   **Return Type**: `Permission` object.

### 5.2 The `Permission` Object
Composed of two powerful constraints:

1.  **`dataRights` (Condition)**: **Row-Level Security**.
    *   This condition is **AND-ed** to every `SELECT`, `UPDATE`, and `DELETE` query.
    *   *Example*: `tenant_id = 'T001'` ensures users never see data from other tenants.

2.  **`fieldRights` (List\<String>)**: **Column-Level Security**.
    *   If not null, only these fields will be returned in `SELECT` statements.
    *   Useful for hiding sensitive fields like `salary` or `password_hash` from generic UI endpoints.

### 5.3 Complete Security Implementation

```java
@Component
public class MySecurityHolder implements CurrentUserHolder {

    @Override
    public String getCurrentUserId() {
        // 1. Get User ID from Security Context
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public Permission getCurrentUserPermission(Model model) {
        String userId = getCurrentUserId();
        String role = getUserRole(userId); // Assume we have this

        // Super Admin: No restrictions
        if ("ADMIN".equals(role)) {
            return null; 
        }

        // Scenario: Multi-Tenancy for "Orders" table
        if ("Order".equals(model.getName())) {
            String tenantId = getUserTenant(userId);
            
            // Row Security: Force tenant_id check
            Condition rowScope = SimpleCondition.eq("tenant_id", tenantId);
            
            return new Permission(null, rowScope);
        }

        // Scenario: Employee Table Visibility
        if ("Employee".equals(model.getName())) {
            // Column Security: Regular users can't see 'salary'
            List<String> allowedFields = Arrays.asList("id", "name", "department");
            
            return new Permission(allowedFields, null);
        }

        return null; // Default: No access or Full access (depending on your policy)
    }
}
```
