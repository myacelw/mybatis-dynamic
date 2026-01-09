package io.github.myacelw.mybatis.dynamic.core.annotation;

import java.lang.annotation.*;

/**
 * 分组字段注解
 *
 * @author liuwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface GroupField {

    boolean select() default true;

    /**
     * 子字段列名前缀
     */
    String columnPrefix() default "";

    // 下面是和数据库字段创建相关属性

    /**
     * 子字段是否需要创建索引总开关; 如果不启用自动DDL则可忽略
     */
    boolean ddlIndexEnabled() default true;

    /**
     * 子字段索引的名称前缀; 如果不启用自动DDL则可忽略
     */
    String ddlIndexNamePrefix() default "";

    /**
     * 子字段是否必填属性总开关; 如果不启用自动DDL则可忽略
     */
    boolean ddlRequiredEnabled() default true;

    /**
     * 子字段列备注的前缀; 如果不启用自动DDL则可忽略
     */
    String ddLCommentPrefix() default "";


    String FIELD_NAME = "__FIELD_NAME__";

}
