package io.github.myacelw.mybatis.dynamic.sample;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.service.ModelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "mybatis-dynamic.ddl.dry-run=true",
        "mybatis-dynamic.ddl.log-path=${java.io.tmpdir}/ddl-integration-tests"
})
public class DDLIntegrationTest {

    @Autowired
    private ModelService modelService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testDryRunAndLogging() throws IOException {
        String logPath = System.getProperty("java.io.tmpdir") + File.separator + "ddl-integration-tests";
        String uniqueTableName = "UNIQUE_DRY_RUN_TABLE_" + System.currentTimeMillis();
        
        Model model = new Model();
        model.setName("DryRunModelIntegration");
        model.setTableName(uniqueTableName);

        BasicField field = new BasicField();
        field.setName("id");
        field.setJavaClass(String.class);
        model.setFields(Collections.singletonList(field));

        // Trigger update
        modelService.update(model);

        // 1. Verify table does NOT exist in DB
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?",
                Integer.class, uniqueTableName);
        assertEquals(0, count, "Table " + uniqueTableName + " should NOT exist in dry-run mode");

        // 2. Verify log file creation
        File logDir = new File(logPath);
        assertTrue(logDir.exists(), "Log directory should exist: " + logPath);
        
        File[] files = logDir.listFiles((dir, name) -> name.startsWith("ddl_") && name.endsWith(".sql"));
        assertNotNull(files);
        assertTrue(files.length > 0, "At least one DDL log file should be created");

        boolean found = false;
        for (File f : files) {
            List<String> lines = Files.readAllLines(f.toPath());
            if (lines.stream().anyMatch(l -> l.contains("CREATE TABLE " + uniqueTableName))) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Log file should contain the CREATE TABLE " + uniqueTableName + " statement");
    }
}
