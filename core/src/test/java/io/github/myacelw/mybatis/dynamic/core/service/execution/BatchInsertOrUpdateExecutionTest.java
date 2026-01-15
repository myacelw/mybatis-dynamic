package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.BatchInsertOrUpdateCommand;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BatchInsertOrUpdateExecutionTest extends BaseExecutionTest {

    @Test
    void exec_BatchInsertOrUpdate() {
        DataManager<String> dataManager = getDataManager("Role");
        BatchInsertOrUpdateExecution<String> execution = new BatchInsertOrUpdateExecution<>();

        List<Map<String, Object>> dataList = new ArrayList<>();
        
        // Update existing r1
        Map<String, Object> r1 = new HashMap<>();
        r1.put("id", "r1");
        r1.put("name", "Updated Role r1");
        dataList.add(r1);

        // Insert new role
        Map<String, Object> newRole = new HashMap<>();
        newRole.put("id", "new_role_1");
        newRole.put("name", "New Role 1");
        dataList.add(newRole);

        BatchInsertOrUpdateCommand command = new BatchInsertOrUpdateCommand();
        command.setData(dataList);

        execution.exec(command, dataManager);

        // Verify r1 updated
        Map<String, Object> role1 = dataManager.getById("r1");
        assertNotNull(role1);
        assertEquals("Updated Role r1", role1.get("name"));

        // Verify new role inserted
        Map<String, Object> nr1 = dataManager.getById("new_role_1");
        assertNotNull(nr1);
        assertEquals("New Role 1", nr1.get("name"));
    }
}
