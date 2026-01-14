package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.AggFunction;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.AggSelectItem;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.AggQueryCommand;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AggQueryExecutionTest extends BaseExecutionTest {

    @Test
    void exec_CountAll() {
        DataManager<String> dataManager = getDataManager("User");
        AggQueryExecution<String, Map<String, Object>> execution = new AggQueryExecution<>();

        AggQueryCommand<Map<String, Object>> command = AggQueryCommand.build();
        command.setAggSelectItems(Collections.singletonList(AggSelectItem.COUNT));

        List<Map<String, Object>> results = execution.exec(command, dataManager);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(((Number) results.get(0).get("count")).intValue() > 0);
    }

    @Test
    void exec_GroupByDepartment() {
        DataManager<String> dataManager = getDataManager("User");
        AggQueryExecution<String, Map<String, Object>> execution = new AggQueryExecution<>();

        AggQueryCommand<Map<String, Object>> command = AggQueryCommand.build();
        // SELECT departmentId, COUNT(*) as count FROM User GROUP BY departmentId
        command.setAggSelectItems(java.util.Arrays.asList(
                AggSelectItem.of("departmentId"),
                AggSelectItem.COUNT
        ));
        
        // This is implicit in AggQueryExecution logic: 
        // if AggSelectItem has NO AggFunction (like departmentId above), it is treated as a GROUP BY field.

        List<Map<String, Object>> results = execution.exec(command, dataManager);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        // Check structure
        Map<String, Object> firstRow = results.get(0);
        assertTrue(firstRow.containsKey("departmentId"));
        assertTrue(firstRow.containsKey("count"));
    }

    @Test
    void exec_MaxName() {
        DataManager<String> dataManager = getDataManager("User");
        AggQueryExecution<String, Map<String, Object>> execution = new AggQueryExecution<>();

        AggQueryCommand<Map<String, Object>> command = AggQueryCommand.build();
        command.setAggSelectItems(Collections.singletonList(
                AggSelectItem.of("name", AggFunction.MAX, "maxName")
        ));

        List<Map<String, Object>> results = execution.exec(command, dataManager);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).containsKey("maxName"));
    }
}