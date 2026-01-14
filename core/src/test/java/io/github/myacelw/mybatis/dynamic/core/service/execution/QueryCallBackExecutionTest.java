package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCallBackCommand;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryCallBackExecutionTest extends BaseExecutionTest {

    @Test
    void exec_QueryCallBackAll() {
        DataManager<String> dataManager = getDataManager("User");
        QueryCallBackExecution<String, Map<String, Object>> execution = new QueryCallBackExecution<>();

        QueryCallBackCommand<Map<String, Object>> command = QueryCallBackCommand.build();
        List<Map<String, Object>> results = new ArrayList<>();
        command.setHandler(new ResultHandler<Map<String, Object>>() {
            @Override
            public void handleResult(ResultContext<? extends Map<String, Object>> resultContext) {
                results.add(resultContext.getResultObject());
            }
        });

        Integer count = execution.exec(command, dataManager);

        assertNotNull(count);
        assertTrue(count >= 2);
        assertEquals(count, results.size());
        
        boolean found = results.stream().anyMatch(u -> "张三".equals(u.get("name")));
        assertTrue(found, "Should find 张三 in callback results");
    }
}