# 4. DataChangeInterceptor

This interface allows you to hook into the data lifecycle. Useful for Auditing, Validation, and Event triggers.

### Usage Scenario: Global Audit Log
You want to record every change to the database.

### Methods
*   **`beforeInsert(DataManager, data)`**: Modify data before insert (e.g., set default UUID).
*   **`afterInsert(DataManager, data, id)`**: Log that a record was created.
*   **`beforeUpdate(DataManager, id, data, fieldValues)`**: Check if user is allowed to update specific fields.
*   **`afterUpdate(...)`**: Log "User X changed Field Y from A to B".
*   **`beforePhysicalDelete` / `beforeLogicDelete`**: Prevent deletion of protected records.

### Implementation Example
```java
@Component
public class AuditInterceptor implements DataChangeInterceptor {

    @Override
    public void afterUpdate(DataManager<Object> dm, Object id, Object data, List<FieldValue> fieldValues) {
        String tableName = dm.getModel().getTableName();
        log.info("Table [{}] Record [{}] updated.", tableName, id);
        
        // Log changed fields
        for (FieldValue fv : fieldValues) {
             log.info("Field [{}] changed to [{}]", fv.getFieldName(), fv.getValue());
        }
    }
}
```
**Registration**: Simply define it as a Spring `@Component`. The framework auto-detects it.

---

