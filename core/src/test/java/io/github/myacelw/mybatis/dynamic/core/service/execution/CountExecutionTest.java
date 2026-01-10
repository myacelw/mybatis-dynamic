package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.CountCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CountExecutionTest extends BaseExecutionTest {

    @Test
    void exec_Count() {
        DataManager<String> dataManager = getDataManager("User");
        CountExecution<String> execution = new CountExecution<>();

        // 1. Count all
        CountCommand commandAll = new CountCommand(null, null);
        Integer countAll = execution.exec(commandAll, dataManager);
        // data.json has 2 users: 张三, 李四
        assertEquals(2, countAll);

        // 2. Count with condition
        Condition condition = SimpleCondition.eq("name", "张三");
        CountCommand commandCondition = new CountCommand(condition, null);
        Integer countCondition = execution.exec(commandCondition, dataManager);
        assertEquals(1, countCondition);
    }
}