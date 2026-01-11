package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.FieldToSqlConverter;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 组条件
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
public class GroupCondition implements Condition {

    Logic logic = Logic.AND;

    List<Condition> conditions = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(logic == null ? Logic.AND.name() : logic.name());
        if (conditions != null && !conditions.isEmpty()) {
            sb.append("(");
            boolean first = true;
            for (Condition c : conditions) {
                if (c != null) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(c);
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }

    @JsonIgnore
    public List<Condition> getC() {
        return conditions;
    }

    @Override
    public GroupCondition clone() {
        try {
            return (GroupCondition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


    public GroupCondition(Logic logic, Collection<? extends Condition> conditions) {
        this.logic = logic;
        if (conditions != null) {
            this.conditions.addAll(conditions);
        }
    }

    public String sql(String valueExpression, FieldToSqlConverter fieldToSqlConverter, DataBaseDialect dialect) {
        if (conditions == null || conditions.isEmpty()) {
            return "";
        }

        String dot = StringUtil.hasText(valueExpression) ? "." : "";

        List<String> list = new ArrayList<>();
        for (int i = 0; i < conditions.size(); i++) {
            Condition c = conditions.get(i);
            if (c != null) {
                String s = c.sql(valueExpression + dot + "c[" + i + "]", fieldToSqlConverter, dialect);
                list.add(s);
            }
        }

        return sql(logic, list);
    }

    @Override
    public Boolean match(@NonNull Object data) {
        Set<Boolean> results = conditions.stream().filter(Objects::nonNull).map(c -> c.match(data)).filter(Objects::nonNull).collect(Collectors.toSet());
        if (results.isEmpty()) {
            return null;
        }
        if (logic == Logic.OR) {
            return results.contains(true);
        } else {
            return !results.contains(false);
        }
    }

    @Override
    public List<String> innerGetSimpleConditionFields() {
        if (conditions == null || conditions.isEmpty()) {
            return Collections.emptyList();
        }
        return conditions.stream().filter(Objects::nonNull).flatMap(c -> c.innerGetSimpleConditionFields().stream()).collect(Collectors.toList());
    }

    @Override
    public List<String> innerGetExistsConditionFields() {
        if (conditions == null || conditions.isEmpty()) {
            return Collections.emptyList();
        }
        return conditions.stream().filter(Objects::nonNull).flatMap(c -> c.innerGetExistsConditionFields().stream()).collect(Collectors.toList());
    }

    public static GroupCondition and(Condition... conditions) {
        return new GroupCondition(Logic.AND, Arrays.asList(conditions));
    }

    public static GroupCondition or(Condition... conditions) {
        return new GroupCondition(Logic.OR, Arrays.asList(conditions));
    }

    public static GroupCondition and(Collection<? extends Condition> conditions) {
        return new GroupCondition(Logic.AND, new ArrayList<>(conditions));
    }

    public static GroupCondition or(Collection<? extends Condition> conditions) {
        return new GroupCondition(Logic.OR, new ArrayList<>(conditions));
    }

    private static String sql(Logic logic, List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder sql = new StringBuilder();
        int n = 0;
        for (String s : list) {
            if (StringUtil.hasText(s)) {
                if (n > 0) {
                    sql.append(" ").append(logic).append(" ");
                }
                sql.append(s);
                n++;
            }
        }

        if (n == 0) {
            return "";
        } else if (n == 1) {
            return sql.toString();
        } else {
            return addBracket(sql.toString());
        }
    }

    public static String and(String... conditions) {
        return sql(Logic.AND, Arrays.asList(conditions));
    }

    /**
     * 增加括号辅助函数
     */
    static String addBracket(String s) {
        if (!StringUtil.hasText(s)) {
            return s;
        }
        if (s.startsWith("(") && s.endsWith(")") && !s.substring(1).contains("(")) {
            return s;
        }
        return "(" + s + ")";
    }

    /**
     * 逻辑操作符
     */
    public enum Logic {
        @JsonAlias({"and", "And"})
        AND,
        @JsonAlias({"or", "Or"})
        OR;

    }
}
