package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.UpdateCommand;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UpdateExecutionTest extends BaseExecutionTest {

    @Test
    void exec_Update() {
        DataManager<String> dataManager = getDataManager("User");
        
        // 1. Insert a record first (using the existing user from data.json or inserting new one)
        // initDataMap has "User" key with list of maps. "u1" is id of first user.
        String userId = "u1";
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", userId);
        updateData.put("name", "Updated Name");

        UpdateCommand<String> command = new UpdateCommand<>(updateData);
        
        UpdateExecution<String> execution = new UpdateExecution<>();
        execution.exec(command, dataManager);

        // Verify
        Map<String, Object> updated = dataManager.getById(userId);
        assertNotNull(updated);
        assertEquals("Updated Name", updated.get("name"));
        // Check if other fields remain unchanged (e.g. departmentId)
        assertEquals("d1", updated.get("departmentId"));
    }

    @Test
    void exec_UpdateForce() {
        DataManager<String> dataManager = getDataManager("User");
        String userId = "u2"; // Li Si

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", userId);
        updateData.put("name", "Forced Name");

        UpdateCommand<String> command = new UpdateCommand<>(updateData);
        command.setForce(true);
        
        UpdateExecution<String> execution = new UpdateExecution<>();
        execution.exec(command, dataManager);

        // Verify
        Map<String, Object> updated = dataManager.getById(userId);
        assertNotNull(updated);
        assertEquals("Forced Name", updated.get("name"));
    }
}
