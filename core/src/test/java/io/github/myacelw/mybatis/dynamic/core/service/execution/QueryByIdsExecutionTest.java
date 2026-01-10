package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryByIdsCommand;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryByIdsExecutionTest extends BaseExecutionTest {

    @Test
    void exec_QueryMultipleExistingIds() {
        DataManager<String> dataManager = getDataManager("User");
        QueryByIdsExecution<String, Map<String, Object>> execution = new QueryByIdsExecution<>();

        QueryByIdsCommand<String, Map<String, Object>> command = new QueryByIdsCommand<>();
        command.setIds(Arrays.asList("u1", "u2"));

        List<Map<String, Object>> results = execution.exec(command, dataManager);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> "u1".equals(u.get("id"))));
        assertTrue(results.stream().anyMatch(u -> "u2".equals(u.get("id"))));
    }

    @Test
    void exec_QueryPartialExistingIds() {
        DataManager<String> dataManager = getDataManager("User");
        QueryByIdsExecution<String, Map<String, Object>> execution = new QueryByIdsExecution<>();

        QueryByIdsCommand<String, Map<String, Object>> command = new QueryByIdsCommand<>();
        command.setIds(Arrays.asList("u1", "non-existent"));

        List<Map<String, Object>> results = execution.exec(command, dataManager);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("u1", results.get(0).get("id"));
    }

    @Test
    void exec_QueryNonExistentIds() {
        DataManager<String> dataManager = getDataManager("User");
        QueryByIdsExecution<String, Map<String, Object>> execution = new QueryByIdsExecution<>();

        QueryByIdsCommand<String, Map<String, Object>> command = new QueryByIdsCommand<>();
        command.setIds(Arrays.asList("non-existent-1", "non-existent-2"));

        List<Map<String, Object>> results = execution.exec(command, dataManager);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void exec_QueryEmptyIds_ThrowsException() {
        DataManager<String> dataManager = getDataManager("User");
        QueryByIdsExecution<String, Map<String, Object>> execution = new QueryByIdsExecution<>();

        QueryByIdsCommand<String, Map<String, Object>> command = new QueryByIdsCommand<>();
        command.setIds(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> execution.exec(command, dataManager));
    }
}
