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
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModelServiceDryRunTest {

    @TempDir
    Path tempDir;

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

    @Test
    public void testUpdateWithLogging() throws IOException {
        SqlSessionFactory sqlSessionFactory = TableServiceBuildUtil.createSqlSessionFactory(Database.H2, "log_test");
        String logPath = tempDir.resolve("ddl-logs").toString();
        ModelService modelService = new ModelServiceBuilder(sqlSessionFactory)
                .dryRun(true)
                .logPath(logPath)
                .build();

        Model model = new Model();
        model.setName("LogModel");
        model.setTableName("LOG_TABLE");

        BasicField field = new BasicField();
        field.setName("id");
        field.setJavaClass(String.class);
        model.setFields(Collections.singletonList(field));

        // Update should trigger logging
        modelService.update(model);

        File logDir = new File(logPath);
        assertTrue(logDir.exists(), "Log directory should be created");
        File[] files = logDir.listFiles();
        assertNotNull(files);
        assertTrue(files.length > 0, "DDL log file should be created");
        
        boolean found = false;
        for (File f : files) {
            if (f.getName().startsWith("ddl_") && f.getName().endsWith(".sql")) {
                List<String> lines = Files.readAllLines(f.toPath());
                if (lines.stream().anyMatch(l -> l.contains("CREATE TABLE LOG_TABLE"))) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found, "Log file with CREATE TABLE LOG_TABLE should be found");
    }
}