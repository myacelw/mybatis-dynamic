package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.CountRecursiveCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CountRecursiveExecutionTest extends BaseExecutionTest {

    @Test
    void exec_CountRecursive() {
        DataManager<String> dataManager = getDataManager("Department");
        CountRecursiveExecution<String> execution = new CountRecursiveExecution<>();

        CountRecursiveCommand command = new CountRecursiveCommand();
        command.setInitNodeCondition(SimpleCondition.eq("id", "d1"));
        command.setRecursiveDown(true);

        Integer count = execution.exec(command, dataManager);

        assertEquals(4, count);
    }
}
