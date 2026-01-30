package io.github.myacelw.mybatis.dynamic.spring;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.service.*;
import io.github.myacelw.mybatis.dynamic.core.service.filler.Filler;
import io.github.myacelw.mybatis.dynamic.core.service.handler.ColumnTypeHandler;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelServiceImpl;
import io.github.myacelw.mybatis.dynamic.spring.controller.AbstractController;
import io.github.myacelw.mybatis.dynamic.spring.controller.DynamicModelController;
import io.github.myacelw.mybatis.dynamic.spring.filler.CreatorFiller;
import io.github.myacelw.mybatis.dynamic.spring.filler.ModifierFiller;
import io.github.myacelw.mybatis.dynamic.spring.hook.CurrentUserHolder;
import lombok.Data;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 配置类
 *
 * @author liuwei
 */
@Configuration
@EnableConfigurationProperties(DynamicModelProperties.class)
@Data
public class DynamicModelAutoConfiguration {

    private final DynamicModelProperties properties;

    public DynamicModelAutoConfiguration(DynamicModelProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(ModelService.class)
    public ModelService modelService(SqlSessionFactory sqlSessionFactory, Optional<CurrentUserHolder> currentUserHolder, List<ColumnTypeHandler> columnTypeHandlerList, List<Filler> fillers, List<DataChangeInterceptor> interceptors) {
        ModelService modelService = new ModelServiceBuilder(sqlSessionFactory)
                .rowLimit(properties.getMaxRowLimit())
                .timeoutSeconds(properties.getTimeoutSeconds())
                .tablePrefix(properties.getTablePrefix())
                .indexPrefix(properties.getIndexPrefix())
                .seqPrefix(properties.getSeqPrefix())
                .dialect(getDataBaseDialect())
                .columnTypeHandlers(columnTypeHandlerList)
                .permissionGetter(currentUserHolder.map(t -> (PermissionGetter) t::getCurrentUserPermission).orElse(null))
                .commentAnnotationClass(properties.getCommentAnnotationClass())
                .commentAnnotationFieldName(properties.getCommentAnnotationFieldName())
                .fillers(fillers)
                .interceptors(interceptors)
                .disableAlterComment(properties.getDisableAlterComment())
                .dryRun(properties.getDdl().isDryRun())
                .logPath(properties.getDdl().getLogPath())
                .build();
        return modelService;
    }

    @Bean
    @ConditionalOnMissingBean(DynamicModelController.class)
    @ConditionalOnProperty(prefix = "mybatis-dynamic.rest", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DynamicModelController dynamicModelController(ModelService modelService, ObjectProvider<CurrentUserHolder> currentUserHolderProvider) {
        return new DynamicModelController(modelService, currentUserHolderProvider.getIfAvailable());
    }

    public DataBaseDialect getDataBaseDialect() {
        List<DataBaseDialect> dataBaseList = DataBaseDialect.getInstances();
        String dialect = properties.getDialect();
        if (!StringUtils.hasText(dialect)) {
            return null;
        }
        for (DataBaseDialect db : dataBaseList) {
            if (db.getName().equalsIgnoreCase(dialect)) {
                return db;
            }
        }
        throw new IllegalArgumentException("No matching database type found for: " + dialect);
    }

    /**
     * 创建人和修改人填充器配置
     */
    @Bean
    @ConditionalOnBean(CurrentUserHolder.class)
    public Filler creatorFiller(CurrentUserHolder currentUserHolder) {
        return new CreatorFiller(currentUserHolder);
    }

    @Bean
    @ConditionalOnBean(CurrentUserHolder.class)
    public Filler modifierFiller(CurrentUserHolder currentUserHolder) {
        return new ModifierFiller(currentUserHolder);
    }

    /**
     * 初始化数据处理
     */
    @Bean
    public CommandLineRunner InitModelDataConfiguration(ModelService modelService, SqlSessionFactory sqlSessionFactory) {
        return args -> {
            if (ObjectUtils.isEmpty(properties.getInitDataFiles())) {
                return;
            }
            ModelDataLoader modelDataLoader = new ModelDataLoader(modelService);
            modelDataLoader.initModelData(sqlSessionFactory, properties.getInitDataFiles());
        };
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(GlobalOperationCustomizer.class)
    public static class GlobalOperationCustomizerConfiguration {

        /**
         * 处理AbstractController 的方法 在swagger 中 operationId 重名问题
         */
        @Bean
        public GlobalOperationCustomizer globalOperationCustomizer() {
            Map<String, Integer> counter = new HashMap<>();

            return (operation, method) -> {
                if (AbstractController.class.equals(method.getMethod().getDeclaringClass())) {
                    Class<?> type = (Class<?>) ((ParameterizedType) method.getBeanType().getGenericSuperclass()).getActualTypeArguments()[0];
                    String operationId = method.getMethod().getName() + type.getSimpleName();
                    int n = counter.getOrDefault(operationId, 0);
                    if (n > 0) {
                        operationId = operationId + "_" + n;
                    }
                    counter.put(operationId, n + 1);
                    operation.setOperationId(operationId);
                } else {
                    counter.compute(operation.getOperationId(), (k, v) -> v == null ? 1 : v + 1);
                }
                return operation;
            };
        }

    }

}

