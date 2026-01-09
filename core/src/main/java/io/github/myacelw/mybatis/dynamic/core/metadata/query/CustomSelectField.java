package io.github.myacelw.mybatis.dynamic.core.metadata.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.util.Collections;
import java.util.List;

/**
 * 自定义查询列字段
 *
 * @author liuwei
 */
@Data
public class CustomSelectField {

    public final static String CONTEXT_KEY = "select";

    /**
     * 结果列名
     */
    private String name;

    /**
     * SQL语句模板，如："MATCH ($COL[0]) AGAINST (#{EXPR} IN BOOLEAN MODE)"
     */
    private String sqlTemplate;

    /**
     * 函数中涉及的字段
     */
    private List<String> fields;

    /**
     * 函数参数
     */
    private Object value;

    /**
     * 列值的java类型
     */
    Class<?> javaType;

    /**
     * 列值指定 typeHandler
     */
    TypeHandler<?> typeHandler;

    /**
     * 列值指定 jdbcType
     */
    JdbcType jdbcType;

    @JsonIgnore
    public Object getV() {
        return value;
    }

    public static CustomSelectField of(String name, String sqlTemplate, String field, Class<?> javaType) {
        CustomSelectField customSelectField = new CustomSelectField();
        customSelectField.setName(name);
        customSelectField.setSqlTemplate(sqlTemplate);
        customSelectField.setFields(Collections.singletonList(field));
        customSelectField.setJavaType(javaType);
        return customSelectField;
    }

    public static CustomSelectField of(String name, String sqlTemplate, String field, Object value, Class<?> javaType) {
        CustomSelectField customSelectField = new CustomSelectField();
        customSelectField.setName(name);
        customSelectField.setSqlTemplate(sqlTemplate);
        customSelectField.setFields(Collections.singletonList(field));
        customSelectField.setValue(value);
        customSelectField.setJavaType(javaType);
        return customSelectField;
    }


    public static CustomSelectField of(String name, String sqlTemplate, String field, Object value, Class<?> javaType, TypeHandler<?> typeHandler) {
        CustomSelectField customSelectField = new CustomSelectField();
        customSelectField.setName(name);
        customSelectField.setSqlTemplate(sqlTemplate);
        customSelectField.setFields(Collections.singletonList(field));
        customSelectField.setValue(value);
        customSelectField.setJavaType(javaType);
        customSelectField.setTypeHandler(typeHandler);
        return customSelectField;
    }

    public static CustomSelectField of(String name, String sqlTemplate, List<String> fields, Object value, Class<?> javaType, TypeHandler<?> typeHandler, JdbcType jdbcType) {
        CustomSelectField customSelectField = new CustomSelectField();
        customSelectField.setName(name);
        customSelectField.setSqlTemplate(sqlTemplate);
        customSelectField.setFields(fields);
        customSelectField.setValue(value);
        customSelectField.setJavaType(javaType);
        customSelectField.setTypeHandler(typeHandler);
        customSelectField.setJdbcType(jdbcType);
        return customSelectField;
    }

}