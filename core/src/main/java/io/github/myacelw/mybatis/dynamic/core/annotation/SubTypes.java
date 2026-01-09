package io.github.myacelw.mybatis.dynamic.core.annotation;

import java.lang.annotation.*;

/**
 * 模型实体类或者子表类如果时接口或者抽象类时可用该注解，声明对应的具体类。
 *
 * @author liuwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface SubTypes {

    /**
     * 如果模型为配置了 subTypes，则这里需要填写区分子类类型的存储字段名
     */
    String subTypeFieldName() default "type";

    SubType[] subTypes() default {};

    /**
     * 定义一个子类型，其中 name 为子类名称，会存储到 subTypeFieldName 对应的字段中。
     */
    @interface SubType {
        Class<?> value();

        /**
         * 子类类型名，如果为空，则默认取Class.getSimpleName();
         */
        String name() default "";

    }

}
