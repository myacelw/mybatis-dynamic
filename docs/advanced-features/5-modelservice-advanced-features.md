# 5. ModelService Advanced Features

The `ModelService` interface provides system-level controls, particularly for the automatic DDL (Data Definition Language) generation mechanism.

### 5.1 Dry Run Mode (`isDryRun`)
If you want to inspect what SQL changes the framework *would* make to your database schema without actually applying them (safe mode), use Dry Run.

```java
// Enable Dry Run
modelService.setDryRun(true);

// Trigger model update (normally happens at startup)
modelService.update(User.class);

// Disable Dry Run for normal operation
modelService.setDryRun(false);
```

### 5.2 DDL Logging (`setLogPath`)
You can configure the framework to write the generated `CREATE TABLE` / `ALTER TABLE` statements to a file instead of (or in addition to) executing them. This is excellent for auditing schema changes.

```java
// Set the path for DDL logs
modelService.setLogPath("/var/logs/app/ddl-changes.sql");
```

---

