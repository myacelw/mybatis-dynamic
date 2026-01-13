package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.exception.crud.ConditionParameterException;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.FieldToSqlConverter;
import io.github.myacelw.mybatis.dynamic.core.util.BeanUtil;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 简单条件
 *
 * @author liuwei
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleCondition implements Condition {

    /**
     * 字段名
     */
    String field;

    /**
     * 操作符
     */
    Operation operation = Operation.eq;

    /**
     * 取值
     */
    Object value;

    /**
     * 条件值为null、空字符串、空数组时忽略条件
     */
    boolean ignoreIfValueEmpty = false;

    @Override
    public String toString() {
        return (ignoreIfValueEmpty ? "?" : "") + operation + "(" + field + ", " + value + ")";
    }


    /**
     * 条件值得短写别名
     */
    @JsonIgnore
    public Object getV() {
        return value;
    }

    @Override
    public SimpleCondition clone() {
        try {
            return (SimpleCondition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构造等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition eq(String field, Object value) {
        return new SimpleCondition(field, Operation.eq, value, false);
    }

    /**
     * 构造可选等于条件，当value为null或空字符串时将不加入查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition eqOptional(String field, Object value) {
        return new SimpleCondition(field, Operation.eq, value, true);
    }

    /**
     * 构造小于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition lt(String field, Object value) {
        return new SimpleCondition(field, Operation.lt, value, false);
    }

    /**
     * 构造可选小于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition ltOptional(String field, Object value) {
        return new SimpleCondition(field, Operation.lt, value, true);
    }

    /**
     * 构造小于等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition lte(String field, Object value) {
        return new SimpleCondition(field, Operation.lte, value, false);
    }

    /**
     * 构造可选小于等于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition lteOptional(String field, Object value) {
        return new SimpleCondition(field, Operation.lte, value, true);
    }

    /**
     * 构造大于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition gt(String field, Object value) {
        return new SimpleCondition(field, Operation.gt, value, false);
    }

    /**
     * 构造可选大于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition gtOptional(String field, Object value) {
        return new SimpleCondition(field, Operation.gt, value, true);
    }

    /**
     * 构造大于等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition gte(String field, Object value) {
        return new SimpleCondition(field, Operation.gte, value, false);
    }

    /**
     * 构造可选大于等于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition gteOptional(String field, Object value) {
        return new SimpleCondition(field, Operation.gte, value, true);
    }

    /**
     * 构造不等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     * @return 一个表示不等于条件的 SimpleCondition 对象
     */
    public static SimpleCondition ne(String field, Object value) {
        return new SimpleCondition(field, Operation.ne, value, false);
    }

    /**
     * 构造可选不等于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     * @return 一个表示可选不等于条件的 SimpleCondition 对象
     */
    public static SimpleCondition neOptional(String field, Object value) {
        return new SimpleCondition(field, Operation.ne, value, true);
    }

    /**
     * 构造Like条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition like(String field, String value) {
        return new SimpleCondition(field, Operation.like, value, false);
    }

    /**
     * 构造可选Like条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition likeOptional(String field, String value) {
        return new SimpleCondition(field, Operation.like, value, true);
    }

    /**
     * 包含条件，SQL语句中使用Like查询，条件值字符串两段会加入‘%’
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition contains(String field, String value) {
        return new SimpleCondition(field, Operation.contains, value, false);
    }

    /**
     * 可选包含条件，SQL语句中使用Like查询，条件值字符串两段会加入‘%’，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition containsOptional(String field, String value) {
        return new SimpleCondition(field, Operation.contains, value, true);
    }

    /**
     * 前缀条件，SQL语句中使用Like查询，查询值右侧加入‘%’
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition startsWith(String field, String value) {
        return new SimpleCondition(field, Operation.startsWith, value, false);
    }

    /**
     * 可选前缀条件，SQL语句中使用Like查询，查询值右侧加入‘%’，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition startsWithOptional(String field, String value) {
        return new SimpleCondition(field, Operation.startsWith, value, true);
    }

    /**
     * 后缀条件，SQL语句中使用Like查询，查询值左侧加入‘%’
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition endsWith(String field, String value) {
        return new SimpleCondition(field, Operation.endsWith, value, false);
    }

    /**
     * 可选后缀条件，SQL语句中使用Like查询，查询值左侧加入‘%’，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition endsWithOptional(String field, String value) {
        return new SimpleCondition(field, Operation.endsWith, value, true);
    }

    /**
     * 为null或为空字符串 条件
     *
     * @param field 条件字段名
     */
    public static SimpleCondition isBlank(String field) {
        return new SimpleCondition(field, Operation.isBlank, null, false);
    }

    /**
     * 不为null且不为空字符串 条件
     *
     * @param field 条件字段名
     */
    public static SimpleCondition isNotBlank(String field) {
        return new SimpleCondition(field, Operation.isNotBlank, null, false);
    }


    /**
     * is null 条件
     *
     * @param field 条件字段名
     */
    public static SimpleCondition isNull(String field) {
        return new SimpleCondition(field, Operation.isNull, null, false);
    }

    /**
     * is not null 条件
     *
     * @param field 条件字段名
     */
    public static SimpleCondition isNotNull(String field) {
        return new SimpleCondition(field, Operation.isNotNull, null, false);
    }

    /**
     * in条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition in(String field, List<?> value) {
        return new SimpleCondition(field, Operation.in, value, false);
    }

    /**
     * 可选in条件，当value为null或空数组时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition inOptional(String field, List<?> value) {
        return new SimpleCondition(field, Operation.in, value, true);
    }

    /**
     * not in条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition notIn(String field, List<?> value) {
        return new SimpleCondition(field, Operation.notIn, value, false);
    }

    /**
     * 可选not in条件，当value为null或空数组时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition notInOptional(String field, List<?> value) {
        return new SimpleCondition(field, Operation.notIn, value, true);
    }


    /**
     * 等于或者in条件；如果条件值列表只有一条数据，则使用等于条件，否则使用in条件。
     *
     * @param field 条件字段名
     * @param value 条件值，不能为null或空数组
     */
    public static SimpleCondition eqOrIn(String field, List<?> value) {
        return new SimpleCondition(field, Operation.eqOrIn, value, false);
    }

    /**
     * 可选等于或者in条件；如果条件值列表为null或空条件被忽略，如果只有一条数据则使用等于条件，否则使用in条件。
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public static SimpleCondition eqOrInOptional(String field, List<?> value) {
        return new SimpleCondition(field, Operation.eqOrIn, value, true);
    }

    @Override
    public String sql(String valueExpression, FieldToSqlConverter fieldToSqlConverter, DataBaseDialect dialect) {
        if (!StringUtil.hasText(field)) {
            return "";
        }

        if (ignoreIfValueEmpty && operation.needValueExpression() && ObjectUtil.isEmpty(value)) {
            return "";
        }
        String dot = StringUtil.hasText(valueExpression) ? "." : "";
        return operation.sql(fieldToSqlConverter == null ? field : fieldToSqlConverter.convertColumn(field), valueExpression + dot + "v", value);
    }

    @Override
    public Boolean match(@NonNull Object data) {
        if (!StringUtil.hasText(field)) {
            return null;
        }

        if (ignoreIfValueEmpty && operation.needValueExpression() && ObjectUtil.isEmpty(value)) {
            return null;
        }

        return operation.match(BeanUtil.getProperty(data, field), value);
    }

    @Override
    public List<String> innerGetSimpleConditionFields() {
        if (!StringUtil.hasText(field)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(field);
    }

    /**
     * 操作符
     */
    public enum Operation {
        @JsonAlias({"EQ", "Eq"})
        eq("$COL = #{EXPR}") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                return objectPropertyValue != null && objectPropertyValue.equals(testValue);
            }
        },
        @JsonAlias({"LT", "Lt"})
        lt("$COL < #{EXPR}") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                if (testValue != null && objectPropertyValue instanceof Comparable) {
                    return ((Comparable) objectPropertyValue).compareTo(testValue) < 0;
                }
                return false;
            }
        },
        @JsonAlias({"GT", "Gt"})
        gt("$COL > #{EXPR}") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                if (testValue != null && objectPropertyValue instanceof Comparable) {
                    return ((Comparable) objectPropertyValue).compareTo(testValue) > 0;
                }
                return false;
            }
        },
        @JsonAlias({"LTE", "Lte"})
        lte("$COL <= #{EXPR}") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                if (testValue != null && objectPropertyValue instanceof Comparable) {
                    return ((Comparable) objectPropertyValue).compareTo(testValue) <= 0;
                }
                return false;
            }
        },
        @JsonAlias({"GTE", "Gte"})
        gte("$COL >= #{EXPR}") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                if (testValue != null && objectPropertyValue instanceof Comparable) {
                    return ((Comparable) objectPropertyValue).compareTo(testValue) >= 0;
                }
                return false;
            }
        },
        @JsonAlias({"NE", "Ne"})
        ne("$COL != #{EXPR}") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                return objectPropertyValue != null && !objectPropertyValue.equals(testValue);
            }
        },
        @JsonAlias({"LIKE", "Like"})
        like("$COL LIKE #{EXPR}") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                return objectPropertyValue != null && testValue != null && objectPropertyValue.toString().contains(testValue.toString());
            }
        },
        @JsonAlias({"CONTAINS", "Contains"})
        contains("$COL LIKE CONCAT(CONCAT('%', #{EXPR}), '%')") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                return objectPropertyValue != null && testValue != null && objectPropertyValue.toString().contains(testValue.toString());
            }
        },
        @JsonAlias({"STARTS_WITH", "StartsWith"})
        startsWith("$COL LIKE CONCAT(#{EXPR}, '%')") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                return objectPropertyValue != null && testValue != null && objectPropertyValue.toString().startsWith(testValue.toString());
            }
        },
        @JsonAlias({"ENDS_WITH", "EndsWith"})
        endsWith("$COL LIKE CONCAT('%', #{EXPR})") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                return objectPropertyValue != null && testValue != null && objectPropertyValue.toString().endsWith(testValue.toString());
            }
        },
        @JsonAlias({"IsNotNull", "is_not_null", "IS_NOT_NULL"})
        isNotNull("$COL IS NOT NULL") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                return objectPropertyValue != null;
            }
        },
        @JsonAlias({"IsNull", "is_null", "IS_NULL"})
        isNull("$COL IS NULL") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                return objectPropertyValue == null;
            }
        },
        @JsonAlias({"IsBlank", "is_blank", "IS_BLANK"})
        isBlank("($COL IS NULL OR $COL = '')") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                return objectPropertyValue == null || objectPropertyValue.toString().isEmpty();
            }
        },
        @JsonAlias({"IsNotBlank", "is_not_blank", "IS_NOT_BLANK"})
        isNotBlank("($COL IS NOT NULL AND $COL != '')") {
            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                return objectPropertyValue != null && objectPropertyValue.toString().isEmpty();
            }
        },
        @JsonAlias({"IN", "In"})
        in("EXPR") {
            public String sql(String column, String valueExpression, Object value) {
                if (value == null) {
                    throw new ConditionParameterException("IN查询条件参数不能为null");
                }

                int n;
                if (value instanceof List) {
                    n = ((List<?>) value).size();
                } else if (value.getClass().isArray()) {
                    n = Array.getLength(value);
                } else {
                    throw new ConditionParameterException("IN查询条件参数类型错误，需要List或数组类型，传入值类型为：" + value.getClass().getName());
                }

                if (n == 0) {
                    throw new ConditionParameterException("IN查询条件参数不能为空集合");
                }
                String s = IntStream.range(0, n).mapToObj(i -> "#{" + valueExpression + "[" + i + "]}").collect(Collectors.joining(","));
                return column + " IN (" + s + ")";
            }

            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                if (objectPropertyValue != null && testValue instanceof Collection) {
                    Collection<?> collection = ((Collection<?>) testValue);
                    return collection.contains(objectPropertyValue);
                }
                return false;
            }
        },

        @JsonAlias({"NOT_IN", "NotIn"})
        notIn("EXPR") {
            public String sql(String column, String valueExpression, Object value) {
                if (value == null) {
                    throw new ConditionParameterException("NOT IN查询条件参数不能为null");
                }

                int n;
                if (value instanceof List) {
                    n = ((List<?>) value).size();
                } else if (value.getClass().isArray()) {
                    n = Array.getLength(value);
                } else {
                    throw new ConditionParameterException("NOT IN查询条件参数类型错误，需要List或数组类型，传入值类型为：" + value.getClass().getName());
                }

                if (n == 0) {
                    throw new ConditionParameterException("NOT IN查询条件参数不能为空集合");
                }
                String s = IntStream.range(0, n).mapToObj(i -> "#{" + valueExpression + "[" + i + "]}").collect(Collectors.joining(","));
                return column + " NOT IN (" + s + ")";
            }

            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                if (objectPropertyValue != null && testValue instanceof Collection) {
                    Collection<?> collection = ((Collection<?>) testValue);
                    return collection.contains(objectPropertyValue);
                }
                return false;
            }
        },


        @JsonAlias({"EQ_OR_IN", "EqOrIn"})
        eqOrIn("EXPR") {
            public String sql(String column, String valueExpression, Object value) {
                if (value instanceof Collection) {
                    Collection<?> collection = ((Collection<?>) value);
                    if (collection.isEmpty()) {
                        throw new ConditionParameterException("等于或In查询条件集合参数不能为空");
                    } else if (collection.size() == 1) {
                        return eq.sql(column, valueExpression + "[0]", value);
                    } else {
                        return in.sql(column, valueExpression, value);
                    }
                } else {
                    return eq.sql(column, valueExpression, value);
                }
            }

            @Override
            public boolean match(Object objectPropertyValue, Object testValue) {
                if (testValue instanceof Collection) {
                    return in.match(objectPropertyValue, testValue);
                } else {
                    return eq.match(objectPropertyValue, testValue);
                }
            }
        };

        private final String format;

        Operation(String format) {
            this.format = format;
        }

        public boolean needValueExpression() {
            return format.contains("EXPR");
        }

        public String sql(String column, String valueExpression, Object value) {
            return this.format.replace("EXPR", valueExpression).replace("$COL", column);
        }

        /**
         * 值是否匹配条件
         */
        public abstract boolean match(Object objectPropertyValue, Object testValue);
    }
}
