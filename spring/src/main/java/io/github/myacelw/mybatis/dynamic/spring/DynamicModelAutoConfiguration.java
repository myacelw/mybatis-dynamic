package io.github.myacelw.mybatis.dynamic.spring;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.service.*;
import io.github.myacelw.mybatis.dynamic.core.service.filler.Filler;
import io.github.myacelw.mybatis.dynamic.core.service.handler.ColumnTypeHandler;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelServiceImpl;
import io.github.myacelw.mybatis.dynamic.spring.controller.AbstractController;
import io.github.myacelw.mybatis.dynamic.spring.filler.CreatorFiller;
import io.github.myacelw.mybatis.dynamic.spring.filler.ModifierFiller;
import io.github.myacelw.mybatis.dynamic.spring.hook.CurrentUserHolder;
import lombok.Data;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
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
@ConfigurationProperties("mybatis-dynamic")
@Data
public class DynamicModelAutoConfiguration {

    /**
     * 数据库方言
     */
    private String dialect;

    /**
     * 表前缀
     */
    private String tablePrefix;

    /**
     * 索引前缀
     */
    private String indexPrefix;

    /**
     * 序列前缀
     */
    private String seqPrefix;

    /**
     * 最大返回行数
     */
    private Integer maxRowLimit;

    /**
     * SQL查询超时时间
     */
    private Integer timeoutSeconds;

    /**
     * 注释注解类型，例如填写为 io.swagger.v3.oas.annotations.media.Schema
     */
    private Class<? extends Annotation> commentAnnotationClass;

    /**
     * 注释注解字段名称，例如填写为 title
     */
    private String commentAnnotationFieldName;


    private Boolean updateModel;

    private Boolean disableAlterComment;

    private List<String> initDataFiles;


    @Bean
    @ConditionalOnMissingBean(ModelService.class)
    public ModelService modelService(SqlSessionFactory sqlSessionFactory, Optional<CurrentUserHolder> currentUserHolder, List<ColumnTypeHandler> columnTypeHandlerList, List<Filler> fillers, List<DataChangeInterceptor> interceptors) {
        ModelService modelService = new ModelServiceBuilder(sqlSessionFactory)
                .rowLimit(maxRowLimit)
                .timeoutSeconds(timeoutSeconds)
                .tablePrefix(tablePrefix)
                .indexPrefix(indexPrefix)
                .seqPrefix(seqPrefix)
                .dialect(getDataBaseDialect())
                .columnTypeHandlers(columnTypeHandlerList)
                .permissionGetter(currentUserHolder.map(t -> (PermissionGetter) t::getCurrentUserPermission).orElse(null))
                .commentAnnotationClass(commentAnnotationClass)
                .commentAnnotationFieldName(commentAnnotationFieldName)
                .fillers(fillers)
                .interceptors(interceptors)
                .disableAlterComment(disableAlterComment)
                .build();
        return modelService;
    }

    public DataBaseDialect getDataBaseDialect() {
        List<DataBaseDialect> dataBaseList = DataBaseDialect.getInstances();
        if (!StringUtils.hasText(dialect)) {
            return null;
        }
        for (DataBaseDialect db : dataBaseList) {
            if (db.getName().equalsIgnoreCase(dialect)) {
                return db;
            }
        }
        throw new IllegalArgumentException("没有找到匹配的数据库类型:" + dialect);
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
            if (ObjectUtils.isEmpty(initDataFiles)) {
                return;
            }
            ModelDataLoader modelDataLoader = new ModelDataLoader(modelService);
            modelDataLoader.initModelData(sqlSessionFactory, initDataFiles);
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

