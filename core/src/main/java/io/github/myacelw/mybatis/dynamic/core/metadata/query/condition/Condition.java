package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.FieldToSqlConverter;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 查询条件
 *
 * @author liuwei
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = SimpleCondition.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleCondition.class, name = "simple", names = {"Simple", "SIMPLE"}),
        @JsonSubTypes.Type(value = GroupCondition.class, name = "group", names = {"Group", "GROUP"}),
        @JsonSubTypes.Type(value = ExistsCondition.class, name = "exists", names = {"Exists", "EXISTS"}),
        @JsonSubTypes.Type(value = NotCondition.class, name = "not", names = {"Not", "NOT"}),
        @JsonSubTypes.Type(value = CustomCondition.class, name = "custom", names = {"Custom", "CUSTOM"}),
        @JsonSubTypes.Type(value = SearchCondition.class, name = "search", names = {"Search", "SEARCH"}),
})
public interface Condition extends Cloneable {

    Condition clone();

    /**
     * 生成查询条件sql片段
     *
     * @param valueExpression     条件根取值表达式
     * @param fieldToSqlConverter 字段转换为sql的转换器，比如字段转换为列名
     * @return sql片段
     */
    String sql(String valueExpression, FieldToSqlConverter fieldToSqlConverter, DataBaseDialect dialect);

    /**
     * 测试数据是否满足条件
     *
     * @param data 数据
     * @return 是否满足，返回为空是条件被忽略
     */
    default Boolean match(@NonNull Object data) {
        throw new UnsupportedOperationException();
    }

    /**
     * 框架内部使用该方法
     */
    List<String> innerGetSimpleConditionFields();

    /**
     * 框架内部使用该方法
     */
    default List<String> innerGetExistsConditionFields() {
        return Collections.emptyList();
    }

    /**
     * 条件构建器
     */
    static ConditionBuilder builder() {
        return new ConditionBuilder();
    }


    /**
     * 替换占位符
     * $COL ： 列名
     * EXPR ： 取值表达式
     */
    static String replacePlaceholders(String valueExpression, FieldToSqlConverter fieldToSqlConverter, String sqlTemplate, String[] fields) {
        String sql;

        if (fields == null || fields.length == 0) {
            sql = sqlTemplate;
        } else {
            // 定义正则表达式模式，用于匹配 $COL[x]
            Pattern pattern = Pattern.compile("\\$COL\\[(\\d+)\\]");
            Matcher matcher = pattern.matcher(sqlTemplate);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                int index = Integer.parseInt(matcher.group(1));
                String replacement;
                if (index < fields.length) {
                    replacement = fieldToSqlConverter == null ? fields[index] : fieldToSqlConverter.convertColumn(fields[index]);
                } else {
                    replacement = matcher.group();
                }
                // 手动进行替换操作
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            sql = sb.toString();
        }

        String dot = StringUtil.hasText(valueExpression) ? "." : "";
        return sql.replace("EXPR", valueExpression + dot + "v");
    }

    /**
     * 替换占位符
     * $COL ： 列名
     * EXPR ： 取值表达式
     */
    static String replacePlaceholders(String valueExpression, FieldToSqlConverter fieldToSqlConverter, String sqlTemplate, String field) {
        String sql = field == null ? sqlTemplate : sqlTemplate.replaceAll("\\$COL", fieldToSqlConverter == null ? field : fieldToSqlConverter.convertColumn(field));

        String dot = StringUtil.hasText(valueExpression) ? "." : "";
        return sql.replace("EXPR", valueExpression + dot + "v");
    }

}
