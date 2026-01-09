package io.github.myacelw.mybatis.dynamic.core.annotation;

import java.lang.annotation.*;

/**
 * 扩展属性
 *
 * @author liuwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE})
@Repeatable(ExtProperty.ExtProperties.class)
public @interface ExtProperty {

    String key() default "";

    String value() default "";

    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ExtProperties {

        ExtProperty[] value() default {};
    }


}
