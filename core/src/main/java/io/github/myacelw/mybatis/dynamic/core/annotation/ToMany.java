package io.github.myacelw.mybatis.dynamic.core.annotation;

import java.lang.annotation.*;

/**
 * 对多关系字段。
 * <p>
 * 添加本注解的字段，应该是List类型；如果List泛型指定了类，那么 targetModel 可不填写。
 * <p>
 * 可用于Join查询，简化Join查询的配置。
 *
 * @author liuwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface ToMany {

    /**
     * 关联模型名称, 如果注解字段List泛型指定了类，那么 targetModel 可不填写。
     */
    String targetModel() default "";

    /**
     * 关联的模型上指向当前模型的外键字段名, 默认值为当前模型名加主键名。
     */
    String[] joinTargetFields() default {};

}
