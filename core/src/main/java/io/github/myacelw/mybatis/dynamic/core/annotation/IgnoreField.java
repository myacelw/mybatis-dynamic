package io.github.myacelw.mybatis.dynamic.core.annotation;

import java.lang.annotation.*;

/**
 * 忽略字段，该字段不作为模型字段
 *
 * @author liuwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface IgnoreField {

}
