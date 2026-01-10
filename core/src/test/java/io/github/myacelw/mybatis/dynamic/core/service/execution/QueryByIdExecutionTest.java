package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.exception.data.DataNotFoundException;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryByIdCommand;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryByIdExecutionTest extends BaseExecutionTest {

    @Test
    void exec_QueryExistingId() {
        DataManager<String> dataManager = getDataManager("User");
        QueryByIdExecution<String, Map<String, Object>> execution = new QueryByIdExecution<>();

        QueryByIdCommand<String, Map<String, Object>> command = new QueryByIdCommand<>();
        command.setId("u1");

        Map<String, Object> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertEquals("u1", result.get("id"));
        assertEquals("张三", result.get("name"));
    }

    @Test
    void exec_QueryNonExistentId_ReturnsNull() {
        DataManager<String> dataManager = getDataManager("User");
        QueryByIdExecution<String, Map<String, Object>> execution = new QueryByIdExecution<>();

        QueryByIdCommand<String, Map<String, Object>> command = new QueryByIdCommand<>();
        command.setId("non-existent");
        command.setNullThrowException(false); // default is false? Let's check setter/getter usage if needed, usually defaults to false

        Map<String, Object> result = execution.exec(command, dataManager);

        assertNull(result);
    }

    @Test
    void exec_QueryNonExistentId_ThrowsException() {
        DataManager<String> dataManager = getDataManager("User");
        QueryByIdExecution<String, Map<String, Object>> execution = new QueryByIdExecution<>();

        QueryByIdCommand<String, Map<String, Object>> command = new QueryByIdCommand<>();
        command.setId("non-existent");
        command.setNullThrowException(true);

        assertThrows(DataNotFoundException.class, () -> execution.exec(command, dataManager));
    }
}
