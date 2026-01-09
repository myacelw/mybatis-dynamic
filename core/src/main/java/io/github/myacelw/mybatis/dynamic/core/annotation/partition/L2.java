package io.github.myacelw.mybatis.dynamic.core.annotation.partition;

import io.github.myacelw.mybatis.dynamic.core.metadata.partition.PartitionFactory;

import java.lang.annotation.*;

/**
 * 二级分区配置
 *
 * @author liuwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface L2 {

    String field();

    int key() default 0;

    int hash() default 0;

    Factory factory() default @Factory(factory = PartitionFactory.None.class);

}
