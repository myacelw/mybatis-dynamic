package io.github.myacelw.mybatis.dynamic.core.util;

import io.github.myacelw.mybatis.dynamic.core.metadata.vo.DDLPlan;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.Sql;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DDLFileLoggerTest {

    @TempDir
    Path tempDir;

    private String logPath;

    @BeforeEach
    public void setup() {
        logPath = tempDir.toString();
    }

    @Test
    public void testLogDDL_WhenEmpty_ShouldNotCreateFile() {
        DDLPlan plan = new DDLPlan();
        DDLFileLogger logger = new DDLFileLogger(logPath);
        
        logger.log(plan);
        
        File[] files = new File(logPath).listFiles();
        assertTrue(files == null || files.length == 0, "No file should be created for empty DDL");
    }

    @Test
    public void testLogDDL_WhenNotEmpty_ShouldCreateFile() throws IOException {
        DDLPlan plan = new DDLPlan();
        plan.addSql(new Sql("CREATE TABLE test (ID INT)"));
        DDLFileLogger logger = new DDLFileLogger(logPath);
        
        logger.log(plan);
        
        File[] files = new File(logPath).listFiles();
        assertNotNull(files);
        assertEquals(1, files.length, "One file should be created");
        assertTrue(files[0].getName().startsWith("ddl_"), "Filename should start with ddl_");
        assertTrue(files[0].getName().endsWith(".sql"), "Filename should end with .sql");
        
        List<String> lines = Files.readAllLines(files[0].toPath());
        assertTrue(lines.contains("CREATE TABLE test (ID INT)"), "File should contain the SQL statement");
    }
}
