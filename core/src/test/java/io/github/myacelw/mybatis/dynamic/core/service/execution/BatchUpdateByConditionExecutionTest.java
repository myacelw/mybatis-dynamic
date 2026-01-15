package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.ConditionBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.BatchUpdateByConditionCommand;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BatchUpdateByConditionExecutionTest extends BaseExecutionTest {

    @Test
    void exec_BatchUpdateByCondition() {
        DataManager<String> dataManager = getDataManager("User");
        BatchUpdateByConditionExecution<String> execution = new BatchUpdateByConditionExecution<>();

        List<BatchUpdateByConditionCommand.UpdatePair> updates = new ArrayList<>();

        // Pair 1: Update user 'u1' to have name 'Zhang San Updated'
        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "Zhang San Updated");
        ConditionBuilder cb1 = new ConditionBuilder();
        cb1.eq("id", "u1");
        updates.add(new BatchUpdateByConditionCommand.UpdatePair(cb1.build(), data1, true, null));

        // Pair 2: Update user 'u2' to have departmentId 'd3'
        Map<String, Object> data2 = new HashMap<>();
        data2.put("departmentId", "d3");
        ConditionBuilder cb2 = new ConditionBuilder();
        cb2.eq("id", "u2");
        updates.add(new BatchUpdateByConditionCommand.UpdatePair(cb2.build(), data2, true, null));

        BatchUpdateByConditionCommand command = new BatchUpdateByConditionCommand(updates, 1000);

        execution.exec(command, dataManager);

        // Verify in DB
        Map<String, Object> user1 = dataManager.getById("u1");
        assertNotNull(user1);
        assertEquals("Zhang San Updated", user1.get("name"));

        Map<String, Object> user2 = dataManager.getById("u2");
        assertNotNull(user2);
        assertEquals("d3", user2.get("departmentId"));
    }
}