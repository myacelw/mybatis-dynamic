package io.github.myacelw.mybatis.dynamic.core.database.impl;

import io.github.myacelw.mybatis.dynamic.core.Database;
import io.github.myacelw.mybatis.dynamic.core.TableServiceBuildUtil;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.DDLPlan;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class TableManagerPlanTest {

    @Test
    public void testGetUpdatePlan() {
        TableManagerImpl tableService = TableServiceBuildUtil.createTableService(Database.H2);
        
        Table table = new Table("TEST_PLAN", null);
        Column column = new Column();
        column.setColumnName("ID");
        column.setDataType("VARCHAR");
        column.setCharacterMaximumLength(32);
        table.setColumns(Collections.singletonList(column));
        
        // This method does not exist yet
        DDLPlan plan = tableService.getUpdatePlan(table);
        assertNotNull(plan);
        assertFalse(plan.isEmpty());
        assertTrue(plan.getSqlList().stream().anyMatch(s -> s.getSql().toUpperCase().contains("CREATE TABLE TEST_PLAN")));
    }
}
