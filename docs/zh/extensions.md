# 扩展

MyBatis Dynamic 旨在通过标准 Java 服务提供者接口 (SPI) 进行扩展。这允许你添加对新数据库或自定义命令的支持，而无需修改核心框架。

## 新数据库支持

要添加对新数据库的支持，你需要实现 `DataBaseDialect` 接口。此接口定义了如何生成用于 DDL 操作（创建表、修改列等）的 SQL 以及如何处理特定于数据库的功能。

### 1. 实现方言

创建一个扩展 `AbstractDataBaseDialect` 的类。这个基类提供了通用的实现，因此你只需要覆盖特定于数据库的逻辑。

```java
package com.example.extension.dialect;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.AbstractDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.Sql;

import java.util.Collections;
import java.util.List;

public class MyNewDbDialect extends AbstractDataBaseDialect {

    @Override
    public String getName() {
        return "mydb"; // 配置中使用的键 (mybatis-dynamic.dialect=mydb)
    }

    @Override
    public boolean supportAutoIncrement() {
        return true; // 是否支持自增？
    }

    @Override
    public List<Sql> getCreateTableSql(Table table) {
        // 返回创建表的 SQL
        // 使用 table.getTableName(), table.getColumns() 等
        return Collections.singletonList(new Sql("CREATE TABLE ..."));
    }

    @Override
    public List<Sql> getAddColumnSql(Table table, Column column) {
        // 返回添加列的 SQL
        return Collections.singletonList(new Sql("ALTER TABLE " + table.getTableName() + " ADD " + column.getColumnName() + " ..."));
    }

    // 根据需要覆盖其他方法：
    // getDropTableSql, getRenameTableSql, getDropColumnSql, getAlterColumnTypeSql...
}
```

### 2. 注册服务

在你的 `resources` 文件夹中创建一个名为 `META-INF/services/io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect` 的文件。

**内容：**
```text
com.example.extension.dialect.MyNewDbDialect
```

### 3. 用法

配置你的应用程序以使用新方言。

```yaml
mybatis-dynamic:
  dialect: mydb
```

---

## 自定义命令

你可以通过添加自定义命令来扩展框架的功能。Command 是 `DataManager` 中的工作单元。

### 1. 定义命令

创建一个实现 `Command` 接口的 POJO 类。此类保存你的操作参数。

```java
public class MyCustomCommand implements Command {
    private String customParam;
    // ... getter 和 setter
}
```

### 2. 实现 Execution

创建一个 `Execution<ID, R, C>` 的实现来处理你的命令。
- `ID`: ID 的类型 (例如 Integer, Long)。
- `R`: 命令的返回类型。
- `C`: 你的命令类。

```java
public class MyCustomExecution implements Execution<Long, String, MyCustomCommand> {

    @Override
    public Class<MyCustomCommand> getCommandClass() {
        return MyCustomCommand.class;
    }

    @Override
    public String exec(MyCustomCommand command, DataManager<Long> dataManager) {
        // 在这里实现你的逻辑。
        // 你可以访问 dataManager 来执行数据库操作。
        return "Processed: " + command.getCustomParam();
    }
}
```

### 3. 注册 Execution

在你的 `resources` 文件夹中创建一个名为 `META-INF/services/io.github.myacelw.mybatis.dynamic.core.service.execution.Execution` 的文件。

**内容：**
```text
com.example.extension.execution.MyCustomExecution
```

### 4. 执行

使用 `DataManager` 上的 `execCommand` 方法来运行你的命令。

```java
MyCustomCommand cmd = new MyCustomCommand();
cmd.setCustomParam("Test");

// 框架会自动找到 MyCustomExecution
String result = dataManager.execCommand(cmd); 
```
