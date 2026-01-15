package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryOneRecursiveTreeCommand;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class QueryOneRecursiveTreeExecutionTest extends BaseExecutionTest {

    @Test
    void exec_QueryOneRecursiveTree() {
        DataManager<String> dataManager = getDataManager("Department");
        QueryOneRecursiveTreeExecution<String, Map<String, Object>> execution = new QueryOneRecursiveTreeExecution<>();

        QueryOneRecursiveTreeCommand<Map<String, Object>> command = new QueryOneRecursiveTreeCommand<>();
        command.setInitNodeCondition(SimpleCondition.eq("id", "d1"));

        Map<String, Object> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertEquals("d1", result.get("id"));
        
        List<Map<String, Object>> children = (List<Map<String, Object>>) result.get("children");
        assertNotNull(children);
        assertEquals(2, children.size());
    }
}
