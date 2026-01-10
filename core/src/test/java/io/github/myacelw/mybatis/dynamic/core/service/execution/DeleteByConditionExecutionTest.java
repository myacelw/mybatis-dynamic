package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.DeleteByConditionCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.InsertCommand;
import io.github.myacelw.mybatis.dynamic.core.exception.data.DataNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays; // Added import for Arrays

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeleteByConditionExecutionTest extends BaseExecutionTest {

    private DataManager<String> dataManager;
    private InsertExecution<String> insertExecution;
    private DeleteByConditionExecution<String> deleteByConditionExecution;


    @BeforeEach
    void setUp() {
        super.setUp(); // Call parent setUp to initialize sqlSession and initDataMap
        dataManager = getDataManager("User");
        insertExecution = new InsertExecution<>();
        deleteByConditionExecution = new DeleteByConditionExecution<>();
    }

    @Test
    void exec_PhysicalDelete() {
        // 1. Insert a record for physical deletion
        Map<String, Object> data = new HashMap<>();
        data.put("id", "phys_del_id_1");
        data.put("name", "User to Physical Delete");
        data.put("departmentId", "d1");
        InsertCommand insertCommand = new InsertCommand(data);
        insertCommand.setDisableGenerateId(true);
        insertExecution.exec(insertCommand, dataManager);

        // Verify insertion
        Map<String, Object> inserted = dataManager.getById("phys_del_id_1");
        assertNotNull(inserted);
        assertEquals("User to Physical Delete", inserted.get("name"));

        // 2. Execute physical delete by condition command
        Condition condition = SimpleCondition.eq("name", "User to Physical Delete");
        DeleteByConditionCommand command = new DeleteByConditionCommand(condition);
        command.setForcePhysicalDelete(true); // Force physical delete

        Integer result = deleteByConditionExecution.exec(command, dataManager);
        assertEquals(1, result); // Expect 1 row deleted

        // 3. Verify deletion
        assertThrows(DataNotFoundException.class, () -> {
            dataManager.getById("phys_del_id_1");
        });
    }

    @Test
    void exec_LogicDelete() {
        // 1. Insert a record for logical deletion
        Map<String, Object> data = new HashMap<>();
        data.put("id", "logic_del_id_1");
        data.put("name", "User to Logic Delete");
        data.put("departmentId", "d1");
        InsertCommand insertCommand = new InsertCommand(data);
        insertCommand.setDisableGenerateId(true);
        insertExecution.exec(insertCommand, dataManager);

        // Verify insertion
        Map<String, Object> inserted = dataManager.getById("logic_del_id_1");
        assertNotNull(inserted);
        assertEquals("User to Logic Delete", inserted.get("name"));

        // 2. Execute logical delete by condition command
        Condition condition = SimpleCondition.eq("name", "User to Logic Delete");
        DeleteByConditionCommand command = new DeleteByConditionCommand(condition);
        command.setForcePhysicalDelete(false); // Should default to logical delete if deleteFlag exists

        Integer result = deleteByConditionExecution.exec(command, dataManager);
        assertEquals(1, result); // Expect 1 row affected (updated deleteFlag)

        // 3. Verify logical deletion
        // Should not be found by normal getById
        Map<String, Object> deletedRecordUsingGetById = dataManager.getByIdChain()
                .id("logic_del_id_1")
                .nullNotThrowException()
                .exec();
        assertNull(deletedRecordUsingGetById);

        // Should be found when ignoring logic delete, and deleteFlag should be true
        Map<String, Object> logicallyDeletedRecord = dataManager.queryOneChain()
                .ignoreLogicDelete()
                .where(c -> c.eq("id", "logic_del_id_1"))
                .select("deleteFlag") // Explicitly select deleteFlag
                .exec();
        
        assertNotNull(logicallyDeletedRecord);
        assertEquals(true, logicallyDeletedRecord.get("deleteFlag"));
    }
}

