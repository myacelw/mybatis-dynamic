package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.FieldToSqlConverter;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.util.BeanUtil;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 存在条件
 *
 * @author liuwei
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExistsCondition implements Condition {

    /**
     * 字段名，必须为ToOne 或 ToMany字段
     */
    String field;

    /**
     * 条件
     */
    Condition condition;


    /**
     * 条件
     */
    List<Join> joins;


    /**
     * 不存在的
     */
    boolean not;


    @Override
    public String toString() {
        return (not ? "NOT_" : "") + "EXISTS(" + field + ", " + condition.toString() + ")";
    }

    @JsonIgnore
    public Condition getC() {
        return condition;
    }

    @Override
    public String sql(String valueExpression, FieldToSqlConverter fieldToSqlConverter, DataBaseDialect dialect) {
        if (!StringUtil.hasText(field)) {
            return "";
        }

        FieldToSqlConverter.ConvertExistsSqlResult result = fieldToSqlConverter.convertExistsSql(field, joins);
        String sql = result.getSql();
        FieldToSqlConverter existsConditionConverter = result.getConverter();

        String dot = StringUtil.hasText(valueExpression) ? "." : "";
        String whereSql = condition == null ? null : condition.sql(valueExpression + dot + "c", existsConditionConverter, dialect);

        String notStr = not ? "NOT " : "";

        if (StringUtil.hasText(whereSql)) {
            return notStr + "EXISTS (" + sql + " AND " + whereSql + ")";
        }
        return notStr + "EXISTS (" + sql + ")";
    }

    @Override
    public Boolean match(@NonNull Object data) {
        Object subData = BeanUtil.getProperty(data, field);
        if (subData instanceof Collection) {
            boolean b = ((Collection<?>) subData).stream().anyMatch(condition::match);
            return b != not;
        }
        return false;
    }

    @Override
    public List<String> innerGetSimpleConditionFields() {
        return Collections.emptyList();
    }

    @Override
    public List<String> innerGetExistsConditionFields() {
        if (!StringUtil.hasText(field)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(field);
    }

    public static ExistsCondition of(String field, Condition condition, List<Join> joins) {
        return new ExistsCondition(field, condition, joins, false);
    }

    public static ExistsCondition of(String field, Condition condition, Join... joins) {
        return new ExistsCondition(field, condition, Arrays.asList(joins), false);
    }

    public static ExistsCondition of(String field, Condition condition) {
        return new ExistsCondition(field, condition, null, false);
    }

    public static ExistsCondition notExists(String field, Condition condition, Join... joins) {
        return new ExistsCondition(field, condition, Arrays.asList(joins), true);
    }

    public static ExistsCondition notExists(String field, Condition condition) {
        return new ExistsCondition(field, condition, null, true);
    }


}
