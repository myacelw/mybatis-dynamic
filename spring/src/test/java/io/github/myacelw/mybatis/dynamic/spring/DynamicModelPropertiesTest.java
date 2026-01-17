package io.github.myacelw.mybatis.dynamic.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicModelPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Configuration
    @EnableConfigurationProperties(DynamicModelProperties.class)
    static class TestConfig {
    }

    @Test
    public void testDdlProperties() {
        this.contextRunner.withPropertyValues(
                "mybatis-dynamic.ddl.dry-run=true",
                "mybatis-dynamic.ddl.log-path=./custom-logs"
        ).run((context) -> {
            DynamicModelProperties properties = context.getBean(DynamicModelProperties.class);
            assertThat(properties.getDdl()).isNotNull();
            assertThat(properties.getDdl().isDryRun()).isTrue();
            assertThat(properties.getDdl().getLogPath()).isEqualTo("./custom-logs");
        });
    }

    @Test
    public void testDefaultDdlProperties() {
        this.contextRunner.run((context) -> {
            DynamicModelProperties properties = context.getBean(DynamicModelProperties.class);
            assertThat(properties.getDdl()).isNotNull();
            assertThat(properties.getDdl().isDryRun()).isFalse();
            assertThat(properties.getDdl().getLogPath()).isEqualTo("./ddl-logs");
        });
    }
}