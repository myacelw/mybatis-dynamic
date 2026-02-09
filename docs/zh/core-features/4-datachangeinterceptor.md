# 4. DataChangeInterceptor

此接口允许你挂钩数据生命周期。用于审计、验证和事件触发。

### 使用场景：全局审计日志
你希望记录对数据库的每一次更改。

### 方法
*   **`beforeInsert(DataManager, data)`**：插入前修改数据（例如，设置默认 UUID）。
*   **`afterInsert(DataManager, data, id)`**：记录已创建记录。
*   **`beforeUpdate(DataManager, id, data, fieldValues)`**：检查用户是否允许更新特定字段。
*   **`afterUpdate(...)`**：记录“用户 X 将字段 Y 从 A 更改为 B”。
*   **`beforePhysicalDelete` / `beforeLogicDelete`**：防止删除受保护的记录。

### 实现示例
```java
@Component
public class AuditInterceptor implements DataChangeInterceptor {

    @Override
    public void afterUpdate(DataManager<Object> dm, Object id, Object data, List<FieldValue> fieldValues) {
        String tableName = dm.getModel().getTableName();
        log.info("Table [{}] Record [{}] updated.", tableName, id);
        
        // 记录变更的字段
        for (FieldValue fv : fieldValues) {
             log.info("Field [{}] changed to [{}]", fv.getFieldName(), fv.getValue());
        }
    }
}
```
**注册**：只需将其定义为 Spring `@Component`。框架会自动检测它。

---

