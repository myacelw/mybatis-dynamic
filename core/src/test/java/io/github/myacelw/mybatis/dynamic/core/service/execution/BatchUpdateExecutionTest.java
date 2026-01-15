package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.BatchUpdateCommand;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BatchUpdateExecutionTest extends BaseExecutionTest {

    @Test
    void exec_BatchUpdate() {
        DataManager<String> dataManager = getDataManager("Role");
        BatchUpdateExecution<String> execution = new BatchUpdateExecution<>();

        // Assuming r1 and r2 are initialized from data.json in BaseExecutionTest.setUp
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> r1 = new HashMap<>();
        r1.put("id", "r1");
        r1.put("name", "Updated Role 1");
        dataList.add(r1);

        Map<String, Object> r2 = new HashMap<>();
        r2.put("id", "r2");
        r2.put("name", "Updated Role 2");
        dataList.add(r2);

        BatchUpdateCommand command = new BatchUpdateCommand();
        command.setData(dataList);

        execution.exec(command, dataManager);

        // Verify in DB
        Map<String, Object> role1 = dataManager.getById("r1");
        assertNotNull(role1);
        assertEquals("Updated Role 1", role1.get("name"));

        Map<String, Object> role2 = dataManager.getById("r2");
        assertNotNull(role2);
        assertEquals("Updated Role 2", role2.get("name"));
    }
}
