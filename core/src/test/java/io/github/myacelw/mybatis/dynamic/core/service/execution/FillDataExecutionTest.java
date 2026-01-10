package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.FillDataCommand;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FillDataExecutionTest extends BaseExecutionTest {

    @Test
    void exec_FillToOneData() {
        DataManager<String> dataManager = getDataManager("User");
        FillDataExecution<String> execution = new FillDataExecution<>();

        // u1 has departmentId 'd1' in data.json
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", "u1");
        userMap.put("departmentId", "d1");

        FillDataCommand.FillField fillField = new FillDataCommand.FillField("department");
        FillDataCommand command = new FillDataCommand(Collections.singletonList(userMap), Collections.singletonList(fillField));
        
        execution.exec(command, dataManager);

        assertNotNull(userMap.get("department"));
        Map<String, Object> dept = (Map<String, Object>) userMap.get("department");
        assertEquals("d1", dept.get("id"));
        assertEquals("部门A", dept.get("name"));
    }

    @Test
    void exec_FillToManyData() {
        DataManager<String> dataManager = getDataManager("User");
        FillDataExecution<String> execution = new FillDataExecution<>();

        // u1 has 2 addresses in data.json
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", "u1");

        FillDataCommand.FillField fillField = new FillDataCommand.FillField("userAddressList");
        FillDataCommand command = new FillDataCommand(Collections.singletonList(userMap), Collections.singletonList(fillField));
        
        execution.exec(command, dataManager);

        assertNotNull(userMap.get("userAddressList"));
        List<Map<String, Object>> addresses = (List<Map<String, Object>>) userMap.get("userAddressList");
        assertEquals(2, addresses.size());
    }
}