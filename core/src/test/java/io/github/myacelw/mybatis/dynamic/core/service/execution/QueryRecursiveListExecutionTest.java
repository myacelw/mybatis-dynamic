package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryRecursiveListCommand;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryRecursiveListExecutionTest extends BaseExecutionTest {

    @Test
    void exec_RecursiveDown() {
        DataManager<String> dataManager = getDataManager("Department");
        QueryRecursiveListExecution<String, Map<String, Object>> execution = new QueryRecursiveListExecution<>();

        QueryRecursiveListCommand<Map<String, Object>> command = new QueryRecursiveListCommand<>();
        command.setInitNodeCondition(SimpleCondition.eq("id", "d1"));
        command.setRecursiveDown(true);

        List<Map<String, Object>> results = execution.exec(command, dataManager);

        assertNotNull(results);
        assertEquals(4, results.size());
        List<String> ids = results.stream().map(t -> (String) t.get("id")).collect(Collectors.toList());
        assertTrue(ids.contains("d1"));
        assertTrue(ids.contains("d4"));
        assertTrue(ids.contains("d5"));
        assertTrue(ids.contains("d6"));
    }

    @Test
    void exec_RecursiveUp() {
        DataManager<String> dataManager = getDataManager("Department");
        QueryRecursiveListExecution<String, Map<String, Object>> execution = new QueryRecursiveListExecution<>();

        QueryRecursiveListCommand<Map<String, Object>> command = new QueryRecursiveListCommand<>();
        command.setInitNodeCondition(SimpleCondition.eq("id", "d5"));
        command.setRecursiveDown(false);

        List<Map<String, Object>> results = execution.exec(command, dataManager);

        assertNotNull(results);
        assertEquals(3, results.size());
        List<String> ids = results.stream().map(t -> (String) t.get("id")).collect(Collectors.toList());
        assertTrue(ids.contains("d5"));
        assertTrue(ids.contains("d4"));
        assertTrue(ids.contains("d1"));
    }

    private void assertNotNull(Object obj) {
        org.junit.jupiter.api.Assertions.assertNotNull(obj);
    }
}
