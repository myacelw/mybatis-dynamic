package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.exception.data.DataNotFoundException;
import io.github.myacelw.mybatis.dynamic.core.exception.data.NonUniqueDataException;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryOneCommand;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryOneExecutionTest extends BaseExecutionTest {

    @Test
    void exec_QueryOne_Success() {
        DataManager<String> dataManager = getDataManager("User");
        QueryOneExecution<String, Map<String, Object>> execution = new QueryOneExecution<>();

        QueryOneCommand<Map<String, Object>> command = new QueryOneCommand<>();
        command.setCondition(SimpleCondition.eq("id", "u1"));

        Map<String, Object> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertEquals("u1", result.get("id"));
        assertEquals("张三", result.get("name"));
    }

    @Test
    void exec_QueryOne_OnlyFirst() {
        DataManager<String> dataManager = getDataManager("User");
        QueryOneExecution<String, Map<String, Object>> execution = new QueryOneExecution<>();

        QueryOneCommand<Map<String, Object>> command = new QueryOneCommand<>();
        // Query all users, but onlyFirst = true
        command.setOnlyFirst(true);

        Map<String, Object> result = execution.exec(command, dataManager);

        assertNotNull(result);
        // Should return the first user (id depends on default order, but should be one of u1, u2)
        assertNotNull(result.get("id"));
    }

    @Test
    void exec_QueryOne_NonUnique_ThrowsException() {
        DataManager<String> dataManager = getDataManager("User");
        QueryOneExecution<String, Map<String, Object>> execution = new QueryOneExecution<>();

        QueryOneCommand<Map<String, Object>> command = new QueryOneCommand<>();
        // Query all users, onlyFirst = false (default)
        command.setOnlyFirst(false);

        assertThrows(NonUniqueDataException.class, () -> {
            execution.exec(command, dataManager);
        });
    }

    @Test
    void exec_QueryOne_NotFound_ReturnsNull() {
        DataManager<String> dataManager = getDataManager("User");
        QueryOneExecution<String, Map<String, Object>> execution = new QueryOneExecution<>();

        QueryOneCommand<Map<String, Object>> command = new QueryOneCommand<>();
        command.setCondition(SimpleCondition.eq("id", "non-existent"));
        command.setNullThrowException(false);

        Map<String, Object> result = execution.exec(command, dataManager);

        assertNull(result);
    }

    @Test
    void exec_QueryOne_NotFound_ThrowsException() {
        DataManager<String> dataManager = getDataManager("User");
        QueryOneExecution<String, Map<String, Object>> execution = new QueryOneExecution<>();

        QueryOneCommand<Map<String, Object>> command = new QueryOneCommand<>();
        command.setCondition(SimpleCondition.eq("id", "non-existent"));
        command.setNullThrowException(true);

        assertThrows(DataNotFoundException.class, () -> {
            execution.exec(command, dataManager);
        });
    }
}
