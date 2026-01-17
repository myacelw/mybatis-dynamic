package io.github.myacelw.mybatis.dynamic.core.service.impl;

import io.github.myacelw.mybatis.dynamic.core.Database;
import io.github.myacelw.mybatis.dynamic.core.TableServiceBuildUtil;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.TableDefine;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.service.ModelService;
import io.github.myacelw.mybatis.dynamic.core.service.ModelServiceBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ModelServiceDryRunTest {

    @Test
    public void testUpdateDryRun() {
        SqlSessionFactory sqlSessionFactory = TableServiceBuildUtil.createSqlSessionFactory(Database.H2, "dry_run_test");
        ModelService modelService = new ModelServiceBuilder(sqlSessionFactory)
                .dryRun(true)
                .build();

        Model model = new Model();
        model.setName("DryRunModel");
        model.setTableName("DRY_RUN_TABLE");

        BasicField field = new BasicField();
        field.setName("id");
        field.setJavaClass(String.class);
        model.setFields(Collections.singletonList(field));

        // Attempt update in dry-run mode
        modelService.update(model);

        // Verify that the table was NOT created
        boolean tableExists = modelService.getTableManager().getMetaDataHelper().getTable("DRY_RUN_TABLE", null) != null;
        assertFalse(tableExists, "Table should not exist in dry-run mode");
    }
}