package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.FieldToSqlConverter;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * 非条件
 *
 * @author liuwei
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotCondition implements Condition {
    Condition condition;

    @Override
    public String toString() {
        return "NOT(" + condition.toString() + ")";
    }

    @JsonIgnore
    public Condition getC() {
        return condition;
    }

    @Override
    public NotCondition clone() {
        try {
            NotCondition cloned = (NotCondition) super.clone();
            if (this.condition != null) {
                cloned.condition = this.condition.clone();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String sql(String valueExpression, FieldToSqlConverter fieldToSqlConverter, DataBaseDialect dialect) {
        if (condition == null) {
            return "";
        }

        String dot = StringUtil.hasText(valueExpression) ? "." : "";
        String s = condition.sql(valueExpression + dot + "c", fieldToSqlConverter, dialect);

        if (StringUtil.hasText(s)) {
            return GroupCondition.addBracket("NOT " + s);
        }
        return "";
    }

    @Override
    public Boolean match(@NonNull Object data) {
        Boolean b = condition.match(data);
        return b == null ? null : !b;
    }

    @Override
    public List<String> innerGetSimpleConditionFields() {
        if (condition == null) {
            return Collections.emptyList();
        }
        return condition.innerGetSimpleConditionFields();
    }

    @Override
    public List<String> innerGetExistsConditionFields() {
        if (condition == null) {
            return Collections.emptyList();
        }
        return condition.innerGetExistsConditionFields();
    }

    public static NotCondition of(Condition condition) {
        return new NotCondition(condition);
    }
}
