# Advanced: Fully Dynamic Data Modeling without Entity Classes

> Author: Liu Wei

---

## Introduction

In the previous article [Say Goodbye to XML: Dynamic Data Modeling with mybatis-dynamic](introduction-dynamic-modeling.md), we introduced how mybatis-dynamic uses the philosophy of "Model as Truth" to let Java entity classes directly drive database table structures.

However, in scenarios like **Low-Code Platforms**, **Reporting Systems**, or **Dynamic Forms**, we often face more extreme requirements:
- **Table structures are defined by users at runtime**, and cannot be pre-written as Java classes.
- **Fields may be added or removed at any time**, requiring immediate reflection in the database.
- **Data structures are uncertain**, and can only be carried by JSON or Map.

Some people ask: "Can I still use mybatis-dynamic without Entity classes? Do I need to manually construct SQL?"

The answer is: **Absolutely, and the experience remains seamless.**

The core design of mybatis-dynamic completely decouples `Model` (metadata) from `Class` (Java type). Entity classes are just a shortcut to define a Model; you can completely bypass them and operate directly on the Model.

This article will unlock this "hidden skill" to achieve true **Fully Dynamic Modeling**.

---

## 1. Core Concept: From Class to Model

In standard mode, the framework startup process is:
1. Scan Java classes annotated with `@Model`.
2. Parse class structures to generate `Model` objects (containing metadata like table names, fields, types).
3. Generate DDL based on `Model` objects and synchronize with the database.
4. Register `DataManager` to provide CRUD services.

In **Fully Dynamic Mode**, we only need to manually complete step 2—directly construct the `Model` object, and leave the rest to the framework.

---

## 2. Practical Demonstration

Suppose we want to implement a simple "Online Form Designer" where a user creates a "Leave Application Form" (LeaveApplication) containing two fields: "Applicant" (applicant) and "Days" (days).

### 1. Construct Model Metadata

We don't need to write any Java classes. Instead, we construct the `Model` object directly via code:

```java
// 1. Create Model definition
Model model = new Model();
model.setName("LeaveApplication");  // Model name, used for indexing
model.setTableName("t_leave_app");  // Database table name

// 2. Define Primary Key (usually managed by system, can be custom)
model.addLongIdFieldIfNotExist();   // Automatically add Long type ID field

// 3. Add Fields Dynamically
// Applicant Field
BasicField applicantField = Field.stringBuilder("applicant")
        .characterMaximumLength(50)
        .ddlComment("Applicant Name")
        .build();
model.addField(applicantField);

// Days Field
BasicField daysField = Field.integerBuilder("days")
        .ddlDefaultValue("1")
        .ddlComment("Leave Days")
        .build();
model.addField(daysField);

// 4. Add Standard Audit Fields (creator, create time, etc.)
// Note: Requires CreatorFiller/ModifierFiller implementation Bean in Spring container
model.addAuditFieldsIfNotExist();
```

> 💡 **Tip**: In real projects, this logic usually involves parsing JSON configuration from the frontend into a `Model` object.

### 2. Runtime Registration and Table Creation

Once we have the `Model` object, inject `ModelService` to complete registration. The framework will automatically compare with the database and create the table structure.

```java
@Autowired
private ModelService modelService;

public void deployModel(Model model) {
    // Register and update table structure
    // Framework automatically generates: CREATE TABLE t_leave_app (...)
    modelService.updateAndRegister(model); 
    
    System.out.println("Model " + model.getName() + " published!");
}
```

### 3. Data Operations Based on Map

Without entity classes, how do we access data?
mybatis-dynamic's `DataManager` interface natively supports `Map<String, Object>`.

```java
// Get DataManager for dynamic model
// Note: Generic ID type must match model.addLongIdFieldIfNotExist()
DataManager<Long> dataManager = modelService.getDataManager("LeaveApplication", null);

// --- Insert Data ---
Map<String, Object> data = new HashMap<>();
data.put("applicant", "John Doe");
data.put("days", 3);

// Execute Insert (automatically handles PK and audit fields)
Long id = dataManager.insert(data);
System.out.println("New Record ID: " + id);

// --- Query Data ---
// Returns List<Map<String, Object>>
List<Map<String, Object>> list = dataManager.query(c -> c
    .eq("applicant", "John Doe")
    .gt("days", 1)
);

// --- Update Data ---
Map<String, Object> update = new HashMap<>();
update.put("id", id);
update.put("days", 5);
dataManager.update(update);

// --- Page Query ---
PageResult<Map<String, Object>> page = dataManager.page(1, 10, null);
System.out.println("Total: " + page.getTotal());
```

No need for `BaseService` or `Mapper`. Everything is done at runtime.

---

## 3. Advanced Scenario: Model Hot Update

A common requirement in low-code platforms is: a user realizes a field is missing and wants to add a "Reason" (reason) field.

In mybatis-dynamic, this is as simple as modifying a collection:

```java
// 1. Get existing model
Model currentModel = modelService.getModel("LeaveApplication");

// 2. Add new field
BasicField reasonField = Field.stringBuilder("reason")
        .characterMaximumLength(200)
        .ddlComment("Leave Reason")
        .build();
currentModel.addField(reasonField);

// 3. Hot Update
// Framework automatically detects difference and executes: ALTER TABLE t_leave_app ADD COLUMN reason ...
modelService.updateAndRegister(currentModel);

// 4. Use new field immediately
Map<String, Object> newData = new HashMap<>();
newData.put("applicant", "Jane Doe");
newData.put("reason", "World is big, I want to see it"); // New field
dataManager.insert(newData);
```

---

## 4. Dynamic Join Queries

Even without entity classes, as long as `ToOne` / `ToMany` field metadata is configured correctly, `dataManager.query()` will automatically handle JOINs.

```java
// Assume there is another User model
Model userModel = ...; 
modelService.register(userModel);

// Add dynamic association in LeaveApplication
ToOneField userField = Field.toOneBuilder("user")
        .targetModel("User")     // Target model name
        .joinField("applicant", "name") // This table applicant = User.name
        .build();
leaveModel.addField(userField);
modelService.updateAndRegister(leaveModel);

// Query automatically brings out User info
// Result Map contains key "user", which value is also a Map
List<Map<String, Object>> results = dataManager.query(c -> c.eq("days", 5));
```

---

## 5. Production Challenges and Solutions

While fully dynamic modeling looks beautiful, in real high-concurrency, distributed production environments, you must keep a clear head. Unrestricted use can lead to disasters.

### 1. Model Synchronization in Multi-Instance Deployment

mybatis-dynamic's model metadata is stored in **memory** by default. In microservices or multi-Pod scenarios, if Instance A dynamically adds a field and updates the database, Instance B's memory does not know this change.

**Consequence**: Instance B might ignore new data during queries, or lose data during updates because it doesn't know about the new field.

**Solution**:
*   **Introduce Messaging**: When a node updates a model, broadcast a "Model Change Event" via Redis Pub/Sub or MQ.
*   **Listen and Refresh**: Other nodes listen to the event and reload the latest model definition from the database or configuration center to achieve eventual consistency.

### 2. Risk Control of DDL Changes

`modelService.updateAndRegister()` executes `ALTER TABLE` statements underneath. This is fast in development, but in production:

*   **Table Locking**: If a table has tens of millions of rows, adding a column in MySQL (especially older versions) might lock the table for minutes or longer, interrupting business.
*   **Concurrency Conflicts**: If two admins modify the same table structure simultaneously, it may lead to DDL conflicts or overwrites.

**Suggestions**:
*   **Permission Separation**: Restrict dynamic DDL permissions to specific admins.
*   **Asynchronous Execution**: For large table changes, do not wait synchronously in HTTP request threads; suggest submitting tasks to background execution.
*   **Maintenance Window**: Schedule structural changes for core large tables during off-peak hours.

---

## Summary

The fully dynamic capability of mybatis-dynamic makes it a powerful tool for building **Dynamic Data Platforms**.

- **Metadata Driven**: Completely detached from Java compile-time restrictions.
- **Automatic DDL**: No need to manually maintain complex SQL scripts.
- **Unified API**: Map and Entity share the same Fluent API.

Now, you can confidently reply to the Product Manager: "Change the table however you want, configure online, take effect immediately."

---
*For more examples, please refer to the dynamic model test cases in the project `sample` directory.*
