package io.github.myacelw.mybatis.dynamic.core.annotation;

import io.github.myacelw.mybatis.dynamic.core.metadata.enums.AlterOrDropStrategy;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.KeyGeneratorMode;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.lang.annotation.*;

/**
 * ID字段注解
 *
 * @author liuwei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface IdField {
    /**
     * 数据库列名，Basic、Rel类型字段和FieldGroup的子字段使用
     * 空时自动生成
     */
    String columnName() default "";

    /**
     * 自定义类型转换处理器，默认使用系统内置
     */
    Class<? extends TypeHandler> typeHandler() default TypeHandler.class;

    /**
     * JDBC类型，默认自动推断
     */
    JdbcType jdbcType() default JdbcType.UNDEFINED;

    /**
     * 主键生成策略
     */
    KeyGeneratorMode keyGeneratorMode() default KeyGeneratorMode.DEFAULT;

    /**
     * 排序序号，多值主键时，用于指定主键的排序顺序，从0开始。
     */
    int order() default 0;


    // 下面是和数据库字段创建相关属性

    /**
     * 数据库列类型，比如“VARCHAR”、“INTEGER”、“JSON”等，默认自动推断; 如果不启用自动DDL则可忽略
     */
    String ddlColumnType() default "";

    /**
     * 字符串类型最大长度，仅对字符串类型字段有效; 如果不启用自动DDL则可忽略
     */
    int ddlCharacterMaximumLength() default Integer.MIN_VALUE;

    /**
     * 字段注释；如果不启用自动DDL则可忽略
     */
    String ddlComment() default "";

    /**
     * 列变更策略；如果不启用自动DDL则可忽略
     */
    AlterOrDropStrategy ddlColumnAlterStrategy() default AlterOrDropStrategy.ALTER;


}
