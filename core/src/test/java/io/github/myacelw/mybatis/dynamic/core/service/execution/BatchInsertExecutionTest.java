package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.BatchInsertCommand;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchInsertExecutionTest extends BaseExecutionTest {

    @Test
    void exec_BatchInsert() {
        DataManager<String> dataManager = getDataManager("Role");
        BatchInsertExecution<String> execution = new BatchInsertExecution<>();

        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> r1 = new HashMap<>();
        r1.put("id", "batch_r1");
        r1.put("name", "Batch Role 1");
        dataList.add(r1);

        Map<String, Object> r2 = new HashMap<>();
        r2.put("id", "batch_r2");
        r2.put("name", "Batch Role 2");
        dataList.add(r2);

        BatchInsertCommand command = new BatchInsertCommand();
        command.setData(dataList);
        command.setDisableGenerateId(true);

        List<String> ids = execution.exec(command, dataManager);

        assertNotNull(ids);
        assertEquals(2, ids.size());
        assertTrue(ids.contains("batch_r1"));
        assertTrue(ids.contains("batch_r2"));

        // Verify in DB
        Map<String, Object> role1 = dataManager.getById("batch_r1");
        assertNotNull(role1);
        assertEquals("Batch Role 1", role1.get("name"));

        Map<String, Object> role2 = dataManager.getById("batch_r2");
        assertNotNull(role2);
        assertEquals("Batch Role 2", role2.get("name"));
    }
}
