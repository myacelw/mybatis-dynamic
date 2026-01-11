package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCursorCommand;
import org.apache.ibatis.cursor.Cursor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueryCursorExecutionTest extends BaseExecutionTest {

    @Test
    void exec_Cursor() throws Exception {
        DataManager<String> dataManager = getDataManager("User");
        QueryCursorExecution<String, Map<String, Object>> execution = new QueryCursorExecution<>();

        QueryCursorCommand<Map<String, Object>> command = new QueryCursorCommand<>();
        
        try (Cursor<Map<String, Object>> cursor = execution.exec(command, dataManager)) {
            assertNotNull(cursor);
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> row : cursor) {
                results.add(row);
            }
            
            assertEquals(2, results.size());
            assertTrue(results.stream().anyMatch(r -> "张三".equals(r.get("name"))));
            assertTrue(results.stream().anyMatch(r -> "李四".equals(r.get("name"))));
        }
    }
}
