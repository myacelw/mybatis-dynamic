package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.AggFunction;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.AggSelectItem;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.AggQueryCommand;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AggQueryExecutionTest extends BaseExecutionTest {

    @Test
    void exec_SimpleCount() {
        DataManager<String> dataManager = getDataManager("User");
        AggQueryExecution<String, Map<String, Object>> execution = new AggQueryExecution<>();

        AggQueryCommand<Map<String, Object>> command = new AggQueryCommand<>();
        command.setAggSelectItems(Collections.singletonList(
                AggSelectItem.of("id", AggFunction.COUNT, "total")
        ));

        List<Map<String, Object>> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, ((Number) result.get(0).get("total")).longValue());
    }

    @Test
    void exec_GroupBy() {
        DataManager<String> dataManager = getDataManager("User");
        AggQueryExecution<String, Map<String, Object>> execution = new AggQueryExecution<>();

        AggQueryCommand<Map<String, Object>> command = new AggQueryCommand<>();
        command.setAggSelectItems(Arrays.asList(
                AggSelectItem.of("departmentId"), // Group by field
                AggSelectItem.of("id", AggFunction.COUNT, "count")
        ));

        List<Map<String, Object>> result = execution.exec(command, dataManager);

        assertNotNull(result);
        // Zhang San in d1, Li Si in d2.
        assertEquals(2, result.size());
        
        for (Map<String, Object> row : result) {
            String deptId = (String) row.get("departmentId");
            Long count = ((Number) row.get("count")).longValue();
            if ("d1".equals(deptId)) {
                assertEquals(1L, count);
            } else if ("d2".equals(deptId)) {
                assertEquals(1L, count);
            }
        }
    }

    @Test
    void exec_MaxMin() {
        DataManager<String> dataManager = getDataManager("UserExt");
        AggQueryExecution<String, Map<String, Object>> execution = new AggQueryExecution<>();

        AggQueryCommand<Map<String, Object>> command = new AggQueryCommand<>();
        command.setAggSelectItems(Arrays.asList(
                AggSelectItem.of("oldSystemId", AggFunction.MAX, "maxVal"),
                AggSelectItem.of("oldSystemId", AggFunction.MIN, "minVal")
        ));

        List<Map<String, Object>> result = execution.exec(command, dataManager);

        assertNotNull(result);
        assertEquals(1, result.size());
        // u1: 90001, u2: 90002 (as Strings in models.json)
        assertEquals("90002", result.get(0).get("maxVal").toString());
        assertEquals("90001", result.get(0).get("minVal").toString());
    }
}
