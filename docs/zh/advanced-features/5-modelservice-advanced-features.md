# 5. ModelService 高级功能

`ModelService` 接口提供系统级控制，特别是针对自动 DDL（数据定义语言）生成机制。

### 5.1 空运行模式 (`isDryRun`)
如果您想检查框架 *将要* 对数据库 schema 进行哪些 SQL 更改，而不实际应用它们（安全模式），请使用空运行。

```java
// 启用空运行
modelService.setDryRun(true);

// 触发模型更新 (通常发生在启动时)
modelService.update(User.class);

// 禁用空运行以进行正常操作
modelService.setDryRun(false);
```

### 5.2 DDL 日志记录 (`setLogPath`)
您可以配置框架将生成的 `CREATE TABLE` / `ALTER TABLE` 语句写入文件，而不是（或除了）执行它们。这非常适合审计架构更改。

```java
// 设置 DDL 日志路径
modelService.setLogPath("/var/logs/app/ddl-changes.sql");
```

---

