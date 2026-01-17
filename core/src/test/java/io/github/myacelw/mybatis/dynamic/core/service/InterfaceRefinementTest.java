package io.github.myacelw.mybatis.dynamic.core.service;

import io.github.myacelw.mybatis.dynamic.core.service.execution.BaseExecutionTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InterfaceRefinementTest extends BaseExecutionTest {

    @Test
    void testCountRecursiveReturnType() {
        DataManager<String> dataManager = getDataManager("Department");
        // This should fail to compile if countRecursive returns long
        int count = dataManager.countRecursive(c -> c.eq("name", "部门A"), true);
        assertTrue(count >= 0);
    }

    @Test
    void testAggQueryNaming() {
        DataManager<String> dataManager = getDataManager("User");
        // This should compile now
        dataManager.aggQuery(); 
    }
}
