package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.PageResult;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.PageCommand;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageExecutionTest extends BaseExecutionTest {

    @Test
    void exec_Page_FullPage() {
        DataManager<String> dataManager = getDataManager("User");
        PageExecution<String, Map<String, Object>> execution = new PageExecution<>();

        PageCommand<Map<String, Object>> command = new PageCommand<>();
        command.setPage(new Page(1, 1));

        PageResult<Map<String, Object>> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(2, result.getTotal()); // Total users: u1, u2
    }

    @Test
    void exec_Page_FirstPage_Partial() {
        DataManager<String> dataManager = getDataManager("User");
        PageExecution<String, Map<String, Object>> execution = new PageExecution<>();

        PageCommand<Map<String, Object>> command = new PageCommand<>();
        command.setPage(new Page(1, 10)); // size > total data

        PageResult<Map<String, Object>> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals(2, result.getTotal());
    }

    @Test
    void exec_Page_Empty() {
        DataManager<String> dataManager = getDataManager("User");
        PageExecution<String, Map<String, Object>> execution = new PageExecution<>();

        PageCommand<Map<String, Object>> command = new PageCommand<>();
        command.setCondition(SimpleCondition.eq("id", "non-existent"));

        PageResult<Map<String, Object>> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getTotal());
    }

    @Test
    void exec_Page_WithConditions() {
        DataManager<String> dataManager = getDataManager("User");
        PageExecution<String, Map<String, Object>> execution = new PageExecution<>();

        PageCommand<Map<String, Object>> command = new PageCommand<>();
        command.setCondition(SimpleCondition.eq("name", "张三"));
        command.setPage(new Page(1, 10));

        PageResult<Map<String, Object>> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotal());
        assertEquals("u1", result.getData().get(0).get("id"));
    }

    @Test
    void exec_Page_DefaultPage() {
        DataManager<String> dataManager = getDataManager("User");
        PageExecution<String, Map<String, Object>> execution = new PageExecution<>();

        PageCommand<Map<String, Object>> command = new PageCommand<>();
        // No page set, should use default (1, 10)

        PageResult<Map<String, Object>> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals(2, result.getTotal());
    }
}
