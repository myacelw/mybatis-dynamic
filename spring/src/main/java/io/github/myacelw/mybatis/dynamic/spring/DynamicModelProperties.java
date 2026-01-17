package io.github.myacelw.mybatis.dynamic.spring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * mybatis-dynamic 配置属性
 *
 * @author liuwei
 */
@ConfigurationProperties("mybatis-dynamic")
@Data
public class DynamicModelProperties {

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

    /**
     * 是否更新模型到数据库
     */
    private Boolean updateModel;

    /**
     * 是否禁用修改注释
     */
    private Boolean disableAlterComment;

    /**
     * 初始化数据文件列表
     */
    private List<String> initDataFiles;

    /**
     * DDL 配置
     */
    private Ddl ddl = new Ddl();

    @Data
    public static class Ddl {
        /**
         * 是否开启干跑模式（不实际执行SQL）
         */
        private boolean dryRun = false;

        /**
         * DDL日志保存路径
         */
        private String logPath = "./ddl-logs";
    }
}
