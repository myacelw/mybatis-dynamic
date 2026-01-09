package io.github.myacelw.mybatis.dynamic.core.annotation;

import java.lang.annotation.*;

/**
 * 对一关系字段。
 * 添加本注解的字段，应该是指向另外一个模型实体类。
 * 可用于Join查询，简化Join查询的配置。
 *
 * @author liuwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface ToOne {

    /**
     * 目标模型类名，默认值会从字段类型中获取。
     */
    String targetModel() default "";

    /**
     * 关联模型字段名称, 默认为目标模型名加主键名。
     */
    String[] joinLocalFields() default {};


}
