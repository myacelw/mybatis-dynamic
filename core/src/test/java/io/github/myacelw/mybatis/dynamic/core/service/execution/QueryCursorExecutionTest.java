package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCursorCommand;
import org.apache.ibatis.cursor.Cursor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryCursorExecutionTest extends BaseExecutionTest {

    @Test
    void exec_QueryCursorAll() throws Exception {
        DataManager<String> dataManager = getDataManager("User");
        QueryCursorExecution<String, Map<String, Object>> execution = new QueryCursorExecution<>();

        QueryCursorCommand<Map<String, Object>> command = new QueryCursorCommand<>();

        try (Cursor<Map<String, Object>> cursor = execution.exec(command, dataManager)) {
            assertNotNull(cursor);
            assertFalse(cursor.isOpen()); 
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> row : cursor) {
                results.add(row);
            }

            assertFalse(results.isEmpty());
            assertTrue(results.size() >= 2); 
        }
    }

    @Test
    void exec_QueryCursorWithCondition() throws Exception {
        DataManager<String> dataManager = getDataManager("User");
        QueryCursorExecution<String, Map<String, Object>> execution = new QueryCursorExecution<>();

        QueryCursorCommand<Map<String, Object>> command = new QueryCursorCommand<>();
        command.setCondition(SimpleCondition.eq("name", "张三"));

        try (Cursor<Map<String, Object>> cursor = execution.exec(command, dataManager)) {
            assertNotNull(cursor);
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> row : cursor) {
                results.add(row);
            }

            assertEquals(1, results.size());
            assertEquals("张三", results.get(0).get("name"));
        }
    }

    @Test
    void exec_QueryCursor_OneToMany_ShouldReturnAggregatedRow() throws Exception {
        DataManager<String> dataManager = getDataManager("User");
        QueryCursorExecution<String, Map<String, Object>> execution = new QueryCursorExecution<>();

        QueryCursorCommand<Map<String, Object>> command = new QueryCursorCommand<>();
        // Select user name and userAddressList.address.city. u1 has 2 addresses.
        command.setSelectFields(Arrays.asList("name", "userAddressList.address.city"));
        command.setCondition(SimpleCondition.eq("id", "u1"));

        try (Cursor<Map<String, Object>> cursor = execution.exec(command, dataManager)) {
            assertNotNull(cursor);

            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> row : cursor) {
                results.add(row);
            }

            // Should return 1 row (aggregated), because MyBatis aggregates 1:N into a collection
            assertEquals(1, results.size());
            assertEquals("张三", results.get(0).get("name"));
            
            // Check that the single row contains a collection of addresses
            Object userAddressList = results.get(0).get("userAddressList");
            assertTrue(userAddressList instanceof List);
            List<?> addresses = (List<?>) userAddressList;
            assertEquals(2, addresses.size());
            
            // Verify content of the collection
            assertTrue(addresses.get(0) instanceof Map);
        }
    }
}
