# 5. PermissionGetter & CurrentUserHolder

这是安全引擎。它拦截 **每一个** 数据库查询以注入安全约束。

### 5.1 接口：`CurrentUserHolder`
在 Spring 应用程序中，你实现此接口以将你的安全上下文（Spring Security/Shiro）与 ORM 连接起来。

#### `getCurrentUserId()`
*   **用例**：框架自动使用此 ID 填充 `@BasicField(fill=FillType.INSERT_USER)` 字段。

#### `getCurrentUserPermission(Model model)`
*   **用例**：返回被访问特定模型的权限。
*   **返回类型**：`Permission` 对象。

### 5.2 `Permission` 对象
由两个强大的约束组成：

1.  **`dataRights` (Condition)**：**行级安全**。
    *   此条件被 **AND** 到每个 `SELECT`、`UPDATE` 和 `DELETE` 查询中。
    *   *示例*：`tenant_id = 'T001'` 确保用户永远不会看到来自其他租户的数据。

2.  **`fieldRights` (List\<String>)**：**列级安全**。
    *   如果非空，`SELECT` 语句中将仅返回这些字段。
    *   用于从通用 UI 端点隐藏敏感字段，如 `salary` 或 `password_hash`。

### 5.3 完整的安全实现

```java
@Component
public class MySecurityHolder implements CurrentUserHolder {

    @Override
    public String getCurrentUserId() {
        // 1. 从安全上下文获取用户 ID
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public Permission getCurrentUserPermission(Model model) {
        String userId = getCurrentUserId();
        String role = getUserRole(userId); // 假设我们有这个方法

        // 超级管理员：无限制
        if ("ADMIN".equals(role)) {
            return null; 
        }

        // 场景：“订单”表的多租户
        if ("Order".equals(model.getName())) {
            String tenantId = getUserTenant(userId);
            
            // 行级安全：强制 tenant_id 检查
            Condition rowScope = SimpleCondition.eq("tenant_id", tenantId);
            
            return new Permission(null, rowScope);
        }

        // 场景：员工表可见性
        if ("Employee".equals(model.getName())) {
            // 列级安全：普通用户看不到 'salary'
            List<String> allowedFields = Arrays.asList("id", "name", "department");
            
            return new Permission(allowedFields, null);
        }

        return null; // 默认：无访问权限或完全访问权限（取决于你的策略）
    }
}
```
