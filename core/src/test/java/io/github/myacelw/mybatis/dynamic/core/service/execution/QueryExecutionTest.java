package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCommand;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryExecutionTest extends BaseExecutionTest {

    @Test
    void exec_QueryAll() {
        DataManager<String> dataManager = getDataManager("User");
        QueryExecution<String, Map<String, Object>> execution = new QueryExecution<>();

        QueryCommand<Map<String, Object>> command = new QueryCommand<>();

        List<Map<String, Object>> results = execution.exec(command, dataManager);

        assertNotNull(results);
        // Assuming data.json initializes some users. 
        assertFalse(results.isEmpty());
        
        // Verify one of the users (u1 from data.json)
        boolean found = results.stream().anyMatch(u -> "u1".equals(u.get("id")));
        assertTrue(found, "Should contain u1 user");
    }

    @Test
    void exec_QueryWithCondition() {
        DataManager<String> dataManager = getDataManager("User");
        QueryExecution<String, Map<String, Object>> execution = new QueryExecution<>();

        QueryCommand<Map<String, Object>> command = new QueryCommand<>();
        // Query users where departmentId = 'd1'
        command.setCondition(SimpleCondition.eq("departmentId", "d1"));

        List<Map<String, Object>> results = execution.exec(command, dataManager);

        assertNotNull(results);
        assertFalse(results.isEmpty());

        for (Map<String, Object> user : results) {
            assertEquals("d1", user.get("departmentId"));
        }
    }

    @Test
    void exec_QueryWithSelectFields() {
        DataManager<String> dataManager = getDataManager("User");
        QueryExecution<String, Map<String, Object>> execution = new QueryExecution<>();

        QueryCommand<Map<String, Object>> command = new QueryCommand<>();
        command.setSelectFields(Arrays.asList("id", "name"));
        // Limit to 1 for check
        command.setPage(new Page(1, 1));

        List<Map<String, Object>> results = execution.exec(command, dataManager);

        assertNotNull(results);
        assertFalse(results.isEmpty());

        Map<String, Object> user = results.get(0);
        assertTrue(user.containsKey("id"));
        assertTrue(user.containsKey("name"));
        // Should NOT contain other fields ideally, but Mybatis return map might contain nulls or just miss keys.
        // Usually implementation returns map with only selected keys.
        assertFalse(user.containsKey("departmentId"), "Should not contain unselected fields");
    }

    @Test
    void exec_QueryWithPagination() {
        DataManager<String> dataManager = getDataManager("User");
        QueryExecution<String, Map<String, Object>> execution = new QueryExecution<>();

        // First, get total count to verify logic
        int total = dataManager.count();
        
        if (total > 1) {
            QueryCommand<Map<String, Object>> command = new QueryCommand<>();
            command.setOrderItems(Collections.singletonList(OrderItem.asc("id"))); // Ensure deterministic order
            command.setPage(new Page(1, 1));
            
            List<Map<String, Object>> page1 = execution.exec(command, dataManager);
            assertEquals(1, page1.size());
            
            command.setPage(new Page(2, 1));
            List<Map<String, Object>> page2 = execution.exec(command, dataManager);
            assertEquals(1, page2.size());
            
            assertFalse(page1.get(0).get("id").equals(page2.get(0).get("id")), "Page 1 and Page 2 should have different records");
        }
    }

    @Test
    void exec_QueryWithOrdering() {
        DataManager<String> dataManager = getDataManager("User");
        QueryExecution<String, Map<String, Object>> execution = new QueryExecution<>();

        QueryCommand<Map<String, Object>> command = new QueryCommand<>();
        command.setOrderItems(Collections.singletonList(OrderItem.desc("id")));
        
        List<Map<String, Object>> results = execution.exec(command, dataManager);
        
        assertNotNull(results);
        if (results.size() > 1) {
            // Check order
            // u2, u1
            assertEquals("u2", results.get(0).get("id"));
            assertEquals("u1", results.get(1).get("id"));
        }
    }
}
