package io.github.myacelw.mybatis.dynamic.core.annotation.partition;

import io.github.myacelw.mybatis.dynamic.core.metadata.partition.PartitionFactory;

import java.lang.annotation.*;

/**
 * 分区配置工厂，用于配置工程类，生成分区配置。
 * 比如List分区和Range分区都需要根据字段的值来生成分区配置，此时可以使用工厂类来生成分区配置。
 * 工厂类需要实现PartitionFactory接口，工厂类的构造函数需要传入一个String[]参数，用于传入工厂类需要的参数。
 *
 * @author liuwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface Factory {
    Class<? extends PartitionFactory> factory();

    String[] params() default {};
}