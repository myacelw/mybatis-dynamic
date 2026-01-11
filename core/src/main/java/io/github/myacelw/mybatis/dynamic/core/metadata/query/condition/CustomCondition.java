package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.FieldToSqlConverter;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义条件。
 * 例如，分词检索示例如下：
 * sqlTemplate = "MATCH ($COL) AGAINST (#{EXPR} IN BOOLEAN MODE)"
 * field = "doc"
 * value = "+贸易"
 *
 * @author liuwei
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomCondition implements Condition {

    /**
     * 字段名
     */
    String field;

    String[] fields;

    /**
     * 条件sql语句模板
     */
    String sqlTemplate;

    /**
     * 取值
     */
    Object value;

    @Override
    public String toString() {
        return "CUSTOM(" + sqlTemplate + ", " + (field == null ? Arrays.toString(fields) : field) + ", " + value + ")";
    }

    @JsonIgnore
    public Object getV() {
        return value;
    }

    @Override
    public CustomCondition clone() {
        try {
            CustomCondition cloned = (CustomCondition) super.clone();
            if (this.fields != null) {
                cloned.fields = this.fields.clone();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String sql(String valueExpression, FieldToSqlConverter fieldToSqlConverter, DataBaseDialect dialect) {
        String sql = fields == null || fields.length == 0 ? sqlTemplate : Condition.replacePlaceholders(valueExpression, fieldToSqlConverter, sqlTemplate, fields);
        return field == null ? sql : Condition.replacePlaceholders(valueExpression, fieldToSqlConverter, sql, field);
    }

    @Override
    public List<String> innerGetSimpleConditionFields() {
        List<String> result = new ArrayList<>();

        if (StringUtil.hasText(field)) {
            result.add(field);
        }
        if (fields != null) {
            result.addAll(Arrays.asList(fields));
        }
        return result;
    }

    public static CustomCondition of(String sqlTemplate, String field, Object value) {
        CustomCondition condition = new CustomCondition();
        condition.field = field;
        condition.sqlTemplate = sqlTemplate;
        condition.value = value;
        return condition;
    }


    public static CustomCondition of(String sqlTemplate, String[] fields, Object value) {
        CustomCondition condition = new CustomCondition();
        condition.fields = fields;
        condition.sqlTemplate = sqlTemplate;
        condition.value = value;
        return condition;
    }

}
