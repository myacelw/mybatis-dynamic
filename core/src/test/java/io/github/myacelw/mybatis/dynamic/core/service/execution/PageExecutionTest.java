package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.PageResult;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.PageCommand;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageExecutionTest extends BaseExecutionTest {

    @Test
    void exec_BasicPagination() {
        DataManager<String> dataManager = getDataManager("User");
        PageExecution<String, Map<String, Object>> execution = new PageExecution<>();

        PageCommand<Map<String, Object>> command = new PageCommand<>();
        command.setPage(new Page(1, 1));
        command.setOrderItems(Collections.singletonList(OrderItem.asc("id")));

        PageResult<Map<String, Object>> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        // We know there are 2 users (u1, u2) from data.json (based on QueryExecutionTest)
        assertEquals(2, result.getTotal());
        assertEquals("u1", result.getData().get(0).get("id"));

        // Page 2
        command.setPage(new Page(2, 1));
        result = execution.exec(command, dataManager);
        assertEquals(1, result.getData().size());
        assertEquals(2, result.getTotal());
        assertEquals("u2", result.getData().get(0).get("id"));
    }

    @Test
    void exec_WithCondition() {
        DataManager<String> dataManager = getDataManager("User");
        PageExecution<String, Map<String, Object>> execution = new PageExecution<>();

        PageCommand<Map<String, Object>> command = new PageCommand<>();
        command.setPage(new Page(1, 10));
        command.setCondition(SimpleCondition.eq("departmentId", "d1"));

        PageResult<Map<String, Object>> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertFalse(result.getData().isEmpty());
        // Assuming u1 is in d1. If multiple, count will vary.
        // Let's verify data content
        for (Map<String, Object> user : result.getData()) {
            assertEquals("d1", user.get("departmentId"));
        }
    }

    @Test
    void exec_EmptyResult() {
        DataManager<String> dataManager = getDataManager("User");
        PageExecution<String, Map<String, Object>> execution = new PageExecution<>();

        PageCommand<Map<String, Object>> command = new PageCommand<>();
        command.setPage(new Page(1, 10));
        command.setCondition(SimpleCondition.eq("id", "non-existent"));

        PageResult<Map<String, Object>> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getTotal());
    }
}