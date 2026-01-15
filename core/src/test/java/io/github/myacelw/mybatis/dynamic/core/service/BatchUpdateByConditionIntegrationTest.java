package io.github.myacelw.mybatis.dynamic.core.service;

import io.github.myacelw.mybatis.dynamic.core.event.data.BatchUpdateByConditionDataEvent;
import io.github.myacelw.mybatis.dynamic.core.service.execution.BaseExecutionTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BatchUpdateByConditionIntegrationTest extends BaseExecutionTest {

    @Test
    void testIntegrationWithInterceptorsAndEvents() {
        DataManager<String> dataManager = getDataManager("User");

        AtomicInteger beforeCount = new AtomicInteger(0);
        AtomicInteger afterCount = new AtomicInteger(0);
        AtomicInteger eventCount = new AtomicInteger(0);

        // Add Interceptor
        dataManager.getModelContext().getInterceptor().getInterceptors().add(new DataChangeInterceptor() {
            @Override
            public void beforeBatchUpdateByCondition(DataManager<Object> dm, List<?> updates) {
                beforeCount.incrementAndGet();
                assertEquals(2, updates.size());
            }

            @Override
            public void afterBatchUpdateByCondition(DataManager<Object> dm, List<?> updates) {
                afterCount.incrementAndGet();
                assertEquals(2, updates.size());
            }
        });

        // Add Event Listener
        dataManager.getModelContext().getEventListeners().add(event -> {
            if (event instanceof BatchUpdateByConditionDataEvent) {
                eventCount.incrementAndGet();
                BatchUpdateByConditionDataEvent e = (BatchUpdateByConditionDataEvent) event;
                assertEquals(2, e.getUpdatePairs().size());
            }
        });

        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "Integrate 1");

        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", "Integrate 2");

        dataManager.batchUpdateByConditionChain()
                .add(c -> c.eq("id", "u1"), data1)
                .add(c -> c.eq("id", "u2"), data2)
                .exec();

        // Verify counts
        assertEquals(1, beforeCount.get());
        assertEquals(1, afterCount.get());
        assertEquals(1, eventCount.get());

        // Verify Data
        assertEquals("Integrate 1", dataManager.getById("u1").get("name"));
        assertEquals("Integrate 2", dataManager.getById("u2").get("name"));
    }
}
