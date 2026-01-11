package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCallBackCommand;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryCallBackExecutionTest extends BaseExecutionTest {

    @Test
    @SuppressWarnings("unchecked")
    void exec_CallBack() {
        DataManager<String> dataManager = getDataManager("User");
        QueryCallBackExecution<String, Map<String, Object>> execution = new QueryCallBackExecution<>();

        List<Map<String, Object>> results = new ArrayList<>();
        QueryCallBackCommand<Map<String, Object>> command = new QueryCallBackCommand<>();
        command.setHandler(resultContext -> {
            results.add((Map<String, Object>) resultContext.getResultObject());
        });

        Integer count = execution.exec(command, dataManager);

        assertEquals(2, count);
        assertEquals(2, results.size());
    }
}
