# 3. ModelService

`ModelService` 管理模型本身的生命周期，而不仅仅是数据。

#### `register(Model model)`
*   **功能**：在运行时注册一个新的动态模型。
*   **场景**：用户在 UI 中定义一个新的“表单” -> 你创建一个 `Model` 对象 -> 注册它 -> 框架创建表。

#### `update(Model model)`
*   **功能**：更新数据库模式（DDL）以匹配提供的模型。
*   **行为**：
    *   添加缺失的列。
    *   更新列类型/注释。
    *   创建/更新索引。
    *   **安全**：它通常 *不会* 删除列以防止数据丢失。

#### `getDataManager(Class<?> entityClass)`
*   **功能**：获取用于业务逻辑的 `DataManager` 的工厂方法。
*   **示例**：
    ```java
    DataManager<Long> dm = modelService.getDataManager(Product.class);
    dm.insert(new Product("Phone"));
    ```

---

