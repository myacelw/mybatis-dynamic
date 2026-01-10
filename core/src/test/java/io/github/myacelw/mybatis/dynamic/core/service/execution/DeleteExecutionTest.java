package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.DeleteCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.InsertCommand;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DeleteExecutionTest extends BaseExecutionTest {

    @Test
    void exec_PhysicalDelete() {
        DataManager<String> dataManager = getDataManager("User");
        InsertExecution<String> insertExecution = new InsertExecution<>();
        DeleteExecution<String> deleteExecution = new DeleteExecution<>();

        // 1. Insert a record
        Map<String, Object> data = new HashMap<>();
        data.put("id", "user_to_delete");
        data.put("name", "User to Delete");
        data.put("departmentId", "d1");
        InsertCommand insertCommand = new InsertCommand(data);
        insertCommand.setDisableGenerateId(true);
        insertExecution.exec(insertCommand, dataManager);

        // Verify insertion
        Map<String, Object> inserted = dataManager.getById("user_to_delete");
        assertEquals("User to Delete", inserted.get("name"));

        // 2. Execute physical delete command
        DeleteCommand<String> deleteCommand = new DeleteCommand<>(Collections.singletonList("user_to_delete"));
        deleteCommand.setForcePhysicalDelete(true); // Force physical delete

        Integer result = deleteExecution.exec(deleteCommand, dataManager);
        assertEquals(1, result);

        // 3. Verify deletion
        org.junit.jupiter.api.Assertions.assertThrows(io.github.myacelw.mybatis.dynamic.core.exception.data.DataNotFoundException.class, () -> {
            dataManager.getById("user_to_delete");
        });
    }

    @Test
    void exec_LogicDelete() {
        DataManager<String> dataManager = getDataManager("User");
        InsertExecution<String> insertExecution = new InsertExecution<>();
        DeleteExecution<String> deleteExecution = new DeleteExecution<>();

        // 1. Insert a record
        Map<String, Object> data = new HashMap<>();
        data.put("id", "user_to_logic_delete");
        data.put("name", "User to Logic Delete");
        data.put("departmentId", "d1");
        InsertCommand insertCommand = new InsertCommand(data);
        insertCommand.setDisableGenerateId(true);
        insertExecution.exec(insertCommand, dataManager);

        // Verify insertion
        Map<String, Object> inserted = dataManager.getById("user_to_logic_delete");
        assertEquals("User to Logic Delete", inserted.get("name"));

        // 2. Execute logic delete command
        DeleteCommand<String> deleteCommand = new DeleteCommand<>(Collections.singletonList("user_to_logic_delete"));
        deleteCommand.setForcePhysicalDelete(false); // Default behavior is logic delete if Model has deleteFlag

        Integer result = deleteExecution.exec(deleteCommand, dataManager);
        assertEquals(1, result);

        // 3. Verify logic deletion (deleteFlag updated)
        // getById filters logically deleted records, so it should return null
        Map<String, Object> deletedRecordUsingGetById = dataManager.getByIdChain().id("user_to_logic_delete").nullNotThrowException().exec();
        assertNull(deletedRecordUsingGetById);

        // Retrieve the record ignoring logic delete filter to check deleteFlag
        Map<String, Object> logicallyDeletedRecord = dataManager.queryOneChain()
                .ignoreLogicDelete()
                .where(c -> c.eq("id", "user_to_logic_delete"))
                .select("deleteFlag")
                .exec();
        
        org.junit.jupiter.api.Assertions.assertNotNull(logicallyDeletedRecord);
        //由于逻辑删除字段不会select出来，因此无法判断其取值是否为1.
        assertEquals(true, logicallyDeletedRecord.get("deleteFlag"));
    }
}
