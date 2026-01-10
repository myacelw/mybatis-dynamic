package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.ExistsCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExistsExecutionTest extends BaseExecutionTest {

    @Test
    void exec_Exists() {
        DataManager<String> dataManager = getDataManager("User");
        ExistsExecution<String> execution = new ExistsExecution<>();

        // 1. Should exist
        Condition conditionExists = SimpleCondition.eq("name", "张三");
        ExistsCommand commandExists = new ExistsCommand(conditionExists, null);
        assertTrue(execution.exec(commandExists, dataManager));

        // 2. Should not exist
        Condition conditionNotExists = SimpleCondition.eq("name", "Non Existent User");
        ExistsCommand commandNotExists = new ExistsCommand(conditionNotExists, null);
        assertFalse(execution.exec(commandNotExists, dataManager));
    }
}