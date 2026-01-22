package io.github.myacelw.mybatis.dynamic.core.util;

import io.github.myacelw.mybatis.dynamic.core.metadata.vo.DDLPlan;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.Sql;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DDL 日志记录器，用于将生成的 DDL 语句写入文件。
 *
 * @author liuwei
 */
@Slf4j
public class DDLFileLogger {

    private final String logPath;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public DDLFileLogger(String logPath) {
        this.logPath = logPath;
    }

    public void log(DDLPlan plan) {
        if (plan == null || plan.isEmpty()) {
            return;
        }

        try {
            Path directory = Paths.get(logPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            String timestamp = LocalDateTime.now().format(FORMATTER);
            String fileName = "ddl_" + timestamp + ".sql";
            Path filePath = directory.resolve(fileName);

            List<String> sqls = plan.getSqlList().stream()
                    .map(Sql::getSql)
                    .collect(Collectors.toList());

            Files.write(filePath, sqls, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("DDL statements logged to: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to write DDL logs to directory: {}. Error: {}", logPath, e.getMessage());
        }
    }
}
