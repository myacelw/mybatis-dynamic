package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryRecursiveTreeCommand;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryRecursiveTreeExecutionTest extends BaseExecutionTest {

    @Test
    void exec_QueryRecursiveTree() {
        DataManager<String> dataManager = getDataManager("Department");
        QueryRecursiveTreeExecution<String, Map<String, Object>> execution = new QueryRecursiveTreeExecution<>();

        QueryRecursiveTreeCommand<Map<String, Object>> command = new QueryRecursiveTreeCommand<>();
        command.setInitNodeCondition(SimpleCondition.eq("id", "d1"));

        List<Map<String, Object>> tree = execution.exec(command, dataManager);

        assertNotNull(tree);
        assertEquals(1, tree.size());
        
        Map<String, Object> d1 = tree.get(0);
        assertEquals("d1", d1.get("id"));
        
        List<Map<String, Object>> d1Children = (List<Map<String, Object>>) d1.get("children");
        assertNotNull(d1Children);
        assertEquals(2, d1Children.size()); // d4 and d6
        
        Map<String, Object> d4 = d1Children.stream().filter(t -> "d4".equals(t.get("id"))).findFirst().orElse(null);
        assertNotNull(d4);
        List<Map<String, Object>> d4Children = (List<Map<String, Object>>) d4.get("children");
        assertNotNull(d4Children);
        assertEquals(1, d4Children.size());
        assertEquals("d5", d4Children.get(0).get("id"));
    }
}
