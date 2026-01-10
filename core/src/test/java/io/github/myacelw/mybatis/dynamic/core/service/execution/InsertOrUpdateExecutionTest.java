package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.InsertOrUpdateCommand;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InsertOrUpdateExecutionTest extends BaseExecutionTest {

    @Test
    void exec_InsertWhenNotExists() {
        DataManager<String> dataManager = getDataManager("User");
        InsertOrUpdateExecution<String> execution = new InsertOrUpdateExecution<>();

        Map<String, Object> data = new HashMap<>();
        data.put("id", "new_user_iou");
        data.put("name", "New IOU User");
        data.put("departmentId", "d1");

        InsertOrUpdateCommand command = new InsertOrUpdateCommand(data);
        String id = execution.exec(command, dataManager);

        assertEquals("new_user_iou", id);

        // Verify insertion
        Map<String, Object> inserted = dataManager.getById("new_user_iou");
        assertNotNull(inserted);
        assertEquals("New IOU User", inserted.get("name"));
    }

    @Test
    void exec_UpdateWhenExists() {
        DataManager<String> dataManager = getDataManager("User");
        InsertOrUpdateExecution<String> execution = new InsertOrUpdateExecution<>();

        // u1 already exists
        Map<String, Object> data = new HashMap<>();
        data.put("id", "u1");
        data.put("name", "Updated IOU User");

        InsertOrUpdateCommand command = new InsertOrUpdateCommand(data);
        String id = (String) execution.exec(command, dataManager);

        assertEquals("u1", id);

        // Verify update
        Map<String, Object> updated = dataManager.getById("u1");
        assertNotNull(updated);
        assertEquals("Updated IOU User", updated.get("name"));
        // d1 should still be there
        assertEquals("d1", updated.get("departmentId"));
    }
}
