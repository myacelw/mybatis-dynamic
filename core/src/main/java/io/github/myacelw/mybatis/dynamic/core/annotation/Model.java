package io.github.myacelw.mybatis.dynamic.core.annotation;

import io.github.myacelw.mybatis.dynamic.core.annotation.partition.Partition;

import java.lang.annotation.*;

/**
 * 模型注解
 *
 * @author liuwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Model {

    /**
     * 模型名
     */
    String name() default "";

    /**
     * 是否无需使用@BasicField注解注册字段，默认值为true。
     */
    boolean autoField() default true;

    /**
     * 表注释
     */
    String comment() default "";

    /**
     * 模型对应表名，空时自动生成
     */
    String tableName() default "";

    /**
     * 是否支持逻辑删除
     */
    boolean logicDelete() default false;

    /**
     * 分区配置
     */
    Partition partition() default @Partition(field = "");

    /**
     * 禁用表结构更新，开启后将不会更新表结构
     */
    boolean disableTableCreateAndAlter() default false;

}
