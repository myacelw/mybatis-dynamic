package io.github.myacelw.mybatis.dynamic.core.annotation.partition;

import io.github.myacelw.mybatis.dynamic.core.metadata.partition.PartitionFactory;

import java.lang.annotation.*;

/**
 * 分区配置
 *
 * @author liuwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface Partition {

    String field();

    int key() default 0;

    int hash() default 0;

    /**
     * 使用工厂创建分区
     */
    Factory factory() default @Factory(factory = PartitionFactory.None.class);

    /**
     * 二级分区配置
     */
    L2 level2() default @L2(field = "");

}
