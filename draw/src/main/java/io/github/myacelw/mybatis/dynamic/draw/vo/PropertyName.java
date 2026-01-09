package io.github.myacelw.mybatis.dynamic.draw.vo;

import lombok.Getter;

/**
 * 属性名枚举
 *
 * @author liuwei
 */
@Getter
public enum PropertyName {

    name("名称"),
    fieldType("字段类型"),
    comment("注释"),
    name_comment("名称和注释"),
    tableName("表名"),
    javaClass("Java类型"),
    columnName("列名"),
    columnType("列类型"),
    require("必填"),
    template("是否为模板"),

    ;
    private final String displayName;

    PropertyName(String displayName) {
        this.displayName = displayName;
    }

}
