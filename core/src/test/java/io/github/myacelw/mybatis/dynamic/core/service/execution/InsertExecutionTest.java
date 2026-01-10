package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.InsertCommand;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InsertExecutionTest extends BaseExecutionTest {

    @Test
    void exec_InsertWithId() {
        DataManager<String> dataManager = getDataManager("User");
        InsertExecution<String> execution = new InsertExecution<>();

        Map<String, Object> data = new HashMap<>();
        data.put("id", "user_100");
        data.put("name", "Test User");
        data.put("departmentId", "d1");

        InsertCommand command = new InsertCommand(data);
        command.setDisableGenerateId(true);
        
        String id = (String) execution.exec(command, dataManager);

        assertEquals("user_100", id);

        // Verify in DB
        Map<String, Object> inserted = dataManager.getById("user_100");
        assertNotNull(inserted);
        assertEquals("Test User", inserted.get("name"));
        assertEquals("admin", inserted.get("creator")); // Filled by filler
        assertNotNull(inserted.get("createTime"));
    }

    @Test
    void exec_InsertAutoId() {
        // Assuming "Role" or another model uses Snowflake or Auto increment if configured.
        // Based on models.json, let's check one without explicit ID.
        // User model in data.json has String ID.
        
        DataManager<String> dataManager = getDataManager("User");
        InsertExecution<String> execution = new InsertExecution<>();

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Auto ID User");

        InsertCommand command = new InsertCommand(data);
        
        String id = (String) execution.exec(command, dataManager);

        assertNotNull(id);

        // Verify in DB
        Map<String, Object> inserted = dataManager.getById(id);
        assertNotNull(inserted);
        assertEquals("Auto ID User", inserted.get("name"));
    }
}
