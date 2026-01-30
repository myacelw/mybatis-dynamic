package io.github.myacelw.mybatis.dynamic.draw.vo;

import lombok.Getter;

/**
 * 属性名枚举
 *
 * @author liuwei
 */
@Getter
public enum PropertyName {

    name("Name"),
    fieldType("Field Type"),
    comment("Comment"),
    name_comment("Name and Comment"),
    tableName("Table Name"),
    javaClass("Java Class"),
    columnName("Column Name"),
    columnType("Column Type"),
    notNull("Not Null"),
    template("Is Template"),

    ;
    private final String displayName;

    PropertyName(String displayName) {
        this.displayName = displayName;
    }

}
