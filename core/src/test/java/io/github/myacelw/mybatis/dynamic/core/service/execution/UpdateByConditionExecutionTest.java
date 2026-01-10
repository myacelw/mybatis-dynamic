package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.UpdateByConditionCommand;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UpdateByConditionExecutionTest extends BaseExecutionTest {

    @Test
    void exec_UpdateByCondition() {
        DataManager<String> dataManager = getDataManager("User");
        UpdateByConditionExecution<String> execution = new UpdateByConditionExecution<>();

        // 1. Prepare data: u1, u2 are already in DB from data.json via modelDataLoader
        // Let's verify they exist
        assertNotNull(dataManager.getById("u1"));
        assertNotNull(dataManager.getById("u2"));

        // 2. Execute update by condition
        // Update users in department 'd1' (which is u1)
        Condition condition = SimpleCondition.eq("departmentId", "d1");
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "Updated Department User");

        UpdateByConditionCommand command = new UpdateByConditionCommand(condition, updateData, false, null);
        Integer result = execution.exec(command, dataManager);

        assertEquals(1, result);

        // 3. Verify
        Map<String, Object> updatedU1 = dataManager.getById("u1");
        assertEquals("Updated Department User", updatedU1.get("name"));
        assertEquals(currentUser, updatedU1.get("modifier"));
        assertNotNull(updatedU1.get("updateTime"));

        // u2 should not be updated
        Map<String, Object> u2 = dataManager.getById("u2");
        assertEquals("李四", u2.get("name"));
    }
}