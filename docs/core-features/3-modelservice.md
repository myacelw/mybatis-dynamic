# 3. ModelService

`ModelService` manages the lifecycle of the models themselves, not just the data.

#### `register(Model model)`
*   **Function**: Registers a new dynamic model at runtime.
*   **Scenario**: User defines a new "Form" in the UI -> You create a `Model` object -> Register it -> Framework creates the table.

#### `update(Model model)`
*   **Function**: Updates the database schema (DDL) to match the provided model.
*   **Behavior**:
    *   Adds missing columns.
    *   Updates column types/comments.
    *   Creates/Updates indexes.
    *   **Safe**: It generally does *not* drop columns to prevent data loss.

#### `getDataManager(Class<?> entityClass)`
*   **Function**: The factory method to get the `DataManager` for your business logic.
*   **Example**:
    ```java
    DataManager<Long> dm = modelService.getDataManager(Product.class);
    dm.insert(new Product("Phone"));
    ```

---

