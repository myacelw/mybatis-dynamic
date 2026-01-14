package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCursorCommand;
import org.apache.ibatis.cursor.Cursor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
            assertFalse(cursor.isOpen()); // MyBatis Cursor is not "open" in the JDBC sense until iteration starts, or it might depend on implementation. Actually isOpen() usually returns true if it can be iterated.
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> row : cursor) {
                results.add(row);
            }

            assertFalse(results.isEmpty());
            assertTrue(results.size() >= 2); // Based on data.json having u1, u2 etc.
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
}