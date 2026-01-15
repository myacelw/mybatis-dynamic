package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.execution.BaseExecutionTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BatchUpdateByConditionChainTest extends BaseExecutionTest {

    @Test
    void testFluentBatchUpdateByCondition() {
        DataManager<String> dataManager = getDataManager("User");

        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "User 1 Updated");

        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", "User 2 Updated");

        dataManager.batchUpdateByConditionChain()
                .add(c -> c.eq("id", "u1"), data1)
                .add(c -> c.eq("id", "u2"), data2)
                .exec();

        // Verify
        Map<String, Object> u1 = dataManager.getById("u1");
        assertEquals("User 1 Updated", u1.get("name"));

        Map<String, Object> u2 = dataManager.getById("u2");
        assertEquals("User 2 Updated", u2.get("name"));
    }
}
