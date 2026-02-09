# Extensions

MyBatis Dynamic is designed to be extensible through the standard Java Service Provider Interface (SPI). This allows you to add support for new databases or custom commands without modifying the core framework.

## New Database Support

To add support for a new database, you need to implement the `DataBaseDialect` interface. This interface defines how to generate SQL for DDL operations (create table, alter column, etc.) and handle database-specific features.

### 1. Implement Dialect

Create a class that extends `AbstractDataBaseDialect`. This base class provides common implementations, so you only need to override database-specific logic.

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
        return "mydb"; // Key used in configuration (mybatis-dynamic.dialect=mydb)
    }

    @Override
    public boolean supportAutoIncrement() {
        return true; // Does it support AUTO_INCREMENT?
    }

    @Override
    public List<Sql> getCreateTableSql(Table table) {
        // Return SQL to create table
        // Use table.getTableName(), table.getColumns(), etc.
        return Collections.singletonList(new Sql("CREATE TABLE ..."));
    }

    @Override
    public List<Sql> getAddColumnSql(Table table, Column column) {
        // Return SQL to add a column
        return Collections.singletonList(new Sql("ALTER TABLE " + table.getTableName() + " ADD " + column.getColumnName() + " ..."));
    }

    // Override other methods as needed:
    // getDropTableSql, getRenameTableSql, getDropColumnSql, getAlterColumnTypeSql...
}
```

### 2. Register Service

Create a file named `META-INF/services/io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect` in your `resources` folder.

**Content:**
```text
com.example.extension.dialect.MyNewDbDialect
```

### 3. Usage

Configure your application to use the new dialect.

```yaml
mybatis-dynamic:
  dialect: mydb
```

---

## Custom Commands

You can extend the framework's capabilities by adding custom commands. Commands are the unit of work in `DataManager`.

### 1. Define Command

Create a POJO class that implements the `Command` interface. This class holds the arguments for your operation.

```java
public class MyCustomCommand implements Command {
    private String customParam;
    // ... getters and setters
}
```

### 2. Implement Execution

Create an implementation of `Execution<ID, R, C>` that handles your command.
- `ID`: Type of the ID (e.g., Integer, Long).
- `R`: Return type of the command.
- `C`: Your command class.

```java
public class MyCustomExecution implements Execution<Long, String, MyCustomCommand> {

    @Override
    public Class<MyCustomCommand> getCommandClass() {
        return MyCustomCommand.class;
    }

    @Override
    public String exec(MyCustomCommand command, DataManager<Long> dataManager) {
        // Implement your logic here.
        // You can access dataManager to perform DB operations.
        return "Processed: " + command.getCustomParam();
    }
}
```

### 3. Register Execution

Create a file named `META-INF/services/io.github.myacelw.mybatis.dynamic.core.service.execution.Execution` in your `resources` folder.

**Content:**
```text
com.example.extension.execution.MyCustomExecution
```

### 4. Execute

Use the `execCommand` method on `DataManager` to run your command.

```java
MyCustomCommand cmd = new MyCustomCommand();
cmd.setCustomParam("Test");

// The framework automatically finds MyCustomExecution
String result = dataManager.execCommand(cmd); 
```
