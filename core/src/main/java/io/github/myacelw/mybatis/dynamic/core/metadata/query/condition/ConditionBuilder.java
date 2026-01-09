package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.util.DataUtil;
import io.github.myacelw.mybatis.dynamic.core.util.lambda.SFunction;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.myacelw.mybatis.dynamic.core.util.lambda.LambdaUtil.name;

/**
 * 条件构造器
 */
public class ConditionBuilder {

    private final GroupCondition root = new GroupCondition();

    public GroupCondition build() {
        return this.root;
    }

    private void addInnerBuilder(Consumer<ConditionBuilder> c, Function<ConditionBuilder, Condition> f) {
        ConditionBuilder b = Condition.builder();
        c.accept(b);
        root.getConditions().add(f.apply(b));
    }

    /**
     * 加入条件
     *
     * @param conditions 多个条件
     */
    public ConditionBuilder add(Condition... conditions) {
        for (Condition condition : conditions) {
            root.getConditions().add(condition);
        }
        return this;
    }

    /**
     * 与条件，其中的多个条件使用and拼接
     *
     * @param conditionBuilderConsumer 条件构造器消费者，用于构造条件
     */
    public ConditionBuilder and(Consumer<ConditionBuilder> conditionBuilderConsumer) {
        addInnerBuilder(conditionBuilderConsumer, ConditionBuilder::build);
        return this;
    }

    /**
     * 与条件
     *
     * @param conditions 多个条件
     */
    public ConditionBuilder and(Condition... conditions) {
        root.getConditions().add(GroupCondition.and(conditions));
        return this;
    }

    /**
     * 或条件，其中的多个条件使用and拼接
     *
     * @param conditionBuilderConsumer 条件构造器消费者，用于构造条件
     */
    public ConditionBuilder or(Consumer<ConditionBuilder> conditionBuilderConsumer) {
        addInnerBuilder(conditionBuilderConsumer, b -> {
            b.root.setLogic(GroupCondition.Logic.OR);
            return b.build();
        });
        return this;
    }

    /**
     * 或条件
     *
     * @param conditions 多个条件
     */
    public ConditionBuilder or(Condition... conditions) {
        root.getConditions().add(GroupCondition.or(conditions));
        return this;
    }

    /**
     * 非条件
     *
     * @param conditionBuilderConsumer 条件构造器消费者，用于构造条件
     */
    public ConditionBuilder not(Consumer<ConditionBuilder> conditionBuilderConsumer) {
        addInnerBuilder(conditionBuilderConsumer, b -> NotCondition.of(b.build()));
        return this;
    }

    /**
     * 非条件
     *
     * @param condition 条件
     */
    public ConditionBuilder not(Condition condition) {
        root.getConditions().add(NotCondition.of(condition));
        return this;
    }

    public <T> ConditionBuilder custom(String sqlTemplate, SFunction<T, ?> field, Object value) {
        root.getConditions().add(CustomCondition.of(sqlTemplate, name(field), value));
        return this;
    }

    public ConditionBuilder custom(String sqlTemplate, String field, Object value) {
        root.getConditions().add(CustomCondition.of(sqlTemplate, field, value));
        return this;
    }

    public ConditionBuilder custom(String sqlTemplate, String[] fields, Object value) {
        root.getConditions().add(CustomCondition.of(sqlTemplate, fields, value));
        return this;
    }

    /**
     * 全文检索
     */
    public <T> ConditionBuilder search(SFunction<T, ?> field, Object value) {
        root.getConditions().add(SearchCondition.of(name(field), value));
        return this;
    }

    /**
     * 全文检索Boolean模式
     */
    public <T> ConditionBuilder searchBoolMode(SFunction<T, ?> field, Object value) {
        root.getConditions().add(SearchCondition.ofBoolMode(name(field), value));
        return this;
    }

    /**
     * 全文检索
     */
    public ConditionBuilder search(String field, Object value) {
        root.getConditions().add(SearchCondition.of(field, value));
        return this;
    }

    /**
     * 全文检索Boolean模式
     */
    public ConditionBuilder searchBoolMode(String field, Object value) {
        root.getConditions().add(SearchCondition.ofBoolMode(field, value));
        return this;
    }

    private ConditionBuilder addSimpleCondition(SimpleCondition c, boolean optional) {
        c.setIgnoreIfValueEmpty(optional);
        root.getConditions().add(c);
        return this;
    }

    /**
     * 等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder eq(String field, Object value) {
        return addSimpleCondition(SimpleCondition.eq(field, value), false);
    }

    /**
     * 等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder eq(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.eq(name(field), value), false);
    }

    /**
     * 可选等于条件，当value为null或空字符串时将不加入查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder eqOptional(String field, Object value) {
        return addSimpleCondition(SimpleCondition.eq(field, value), true);
    }

    /**
     * 可选等于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder eqOptional(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.eq(name(field), value), true);
    }

    /**
     * 小于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder lt(String field, Object value) {
        return addSimpleCondition(SimpleCondition.lt(field, value), false);
    }

    /**
     * 小于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder lt(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.lt(name(field), value), false);
    }

    /**
     * 可选小于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder ltOptional(String field, Object value) {
        return addSimpleCondition(SimpleCondition.lt(field, value), true);
    }

    /**
     * 可选小于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder ltOptional(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.lt(name(field), value), true);
    }

    /**
     * 大于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder gt(String field, Object value) {
        return addSimpleCondition(SimpleCondition.gt(field, value), false);
    }

    /**
     * 大于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder gt(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.gt(name(field), value), false);
    }

    /**
     * 可选大于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder gtOptional(String field, Object value) {
        return addSimpleCondition(SimpleCondition.gt(field, value), true);
    }


    /**
     * 可选大于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder gtOptional(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.gt(name(field), value), true);
    }

    /**
     * 不等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder ne(String field, Object value) {
        return addSimpleCondition(SimpleCondition.ne(field, value), false);
    }

    /**
     * 不等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder ne(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.ne(name(field), value), false);
    }

    /**
     * 可选不等于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder neOptional(String field, Object value) {
        return addSimpleCondition(SimpleCondition.ne(field, value), true);
    }

    /**
     * 可选不等于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder neOptional(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.ne(name(field), value), true);
    }

    /**
     * 大于等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder gte(String field, Object value) {
        return addSimpleCondition(SimpleCondition.gte(field, value), false);
    }

    /**
     * 大于等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder gte(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.gte(name(field), value), false);
    }

    /**
     * 可选大于等于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder gteOptional(String field, Object value) {
        return addSimpleCondition(SimpleCondition.gte(field, value), true);
    }

    /**
     * 可选大于等于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder gteOptional(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.gte(name(field), value), true);
    }

    /**
     * 小于等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder lte(String field, Object value) {
        return addSimpleCondition(SimpleCondition.lte(field, value), false);
    }

    /**
     * 小于等于条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder lte(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.lte(name(field), value), false);
    }

    /**
     * 可选小于等于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder lteOptional(String field, Object value) {
        return addSimpleCondition(SimpleCondition.lte(field, value), true);
    }

    /**
     * 可选小于等于条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder lteOptional(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.lte(name(field), value), true);
    }

    /**
     * 构造Like条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder like(String field, String value) {
        return addSimpleCondition(SimpleCondition.like(field, value), false);
    }

    /**
     * 构造Like条件
     *
     * @param field 条件字段
     * @param value 条件值
     */
    public <T> ConditionBuilder like(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.like(name(field), value), false);
    }

    /**
     * 构造可选Like条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder likeOptional(String field, String value) {
        return addSimpleCondition(SimpleCondition.like(field, value), true);
    }

    /**
     * 构造可选Like条件，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段
     * @param value 条件值
     */
    public <T> ConditionBuilder likeOptional(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.like(name(field), value), true);
    }

    /**
     * 包含条件，SQL语句中使用Like查询，条件值字符串会加入‘%’
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder contains(String field, String value) {
        return addSimpleCondition(SimpleCondition.contains(field, value), false);
    }

    /**
     * 包含条件，SQL语句中使用Like查询，条件值字符串两段会加入‘%’
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder contains(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.contains(name(field), value), false);
    }

    /**
     * 可选包含条件，SQL语句中使用Like查询，条件值字符串两段会加入‘%’，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder containsOptional(String field, String value) {
        return addSimpleCondition(SimpleCondition.contains(field, value), true);
    }

    /**
     * 可选包含条件，SQL语句中使用Like查询，条件值字符串两段会加入‘%’，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder containsOptional(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.contains(name(field), value), true);
    }

    /**
     * 前缀条件，SQL语句中使用Like查询，查询值右侧加入‘%’
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder startsWith(String field, String value) {
        return addSimpleCondition(SimpleCondition.startsWith(field, value), false);
    }

    /**
     * 前缀条件，SQL语句中使用Like查询，查询值右侧加入‘%’
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder startsWith(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.startsWith(name(field), value), false);
    }

    /**
     * 可选前缀条件，SQL语句中使用Like查询，查询值右侧加入‘%’，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder startsWithOptional(String field, String value) {
        return addSimpleCondition(SimpleCondition.startsWith(field, value), true);
    }

    /**
     * 可选前缀条件，SQL语句中使用Like查询，查询值右侧加入‘%’，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder startsWithOptional(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.startsWith(name(field), value), true);
    }

    /**
     * 后缀条件，SQL语句中使用Like查询，查询值左侧加入‘%’
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder endsWith(String field, String value) {
        return addSimpleCondition(SimpleCondition.endsWith(field, value), false);
    }

    /**
     * 后缀条件，SQL语句中使用Like查询，查询值左侧加入‘%’
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder endsWith(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.endsWith(name(field), value), false);
    }

    /**
     * 可选后缀条件，SQL语句中使用Like查询，查询值左侧加入‘%’，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder endsWithOptional(String field, String value) {
        return addSimpleCondition(SimpleCondition.endsWith(field, value), true);
    }

    /**
     * 可选后缀条件，SQL语句中使用Like查询，查询值左侧加入‘%’，当value为null或空字符串时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder endsWithOptional(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.endsWith(name(field), value), true);
    }

    /**
     * is null 条件
     *
     * @param field 条件字段名
     */
    public ConditionBuilder isNull(String field) {
        root.getConditions().add(SimpleCondition.isNull(field));
        return this;
    }

    /**
     * is null 条件
     *
     * @param field 条件字段名
     */
    public <T> ConditionBuilder isNull(SFunction<T, ?> field) {
        root.getConditions().add(SimpleCondition.isNull(name(field)));
        return this;
    }

    /**
     * is not null 条件
     *
     * @param field 条件字段名
     */
    public ConditionBuilder isNotNull(String field) {
        root.getConditions().add(SimpleCondition.isNotNull(field));
        return this;
    }

    /**
     * is not null 条件
     *
     * @param field 条件字段名
     */
    public <T> ConditionBuilder isNotNull(SFunction<T, ?> field) {
        root.getConditions().add(SimpleCondition.isNotNull(name(field)));
        return this;
    }

    /**
     * 为null或为空字符串 条件
     *
     * @param field 条件字段名
     */
    public ConditionBuilder isBlank(String field) {
        root.getConditions().add(SimpleCondition.isBlank(field));
        return this;
    }

    /**
     * 为null或为空字符串 条件
     *
     * @param field 条件字段名
     */
    public <T> ConditionBuilder isBlank(SFunction<T, ?> field) {
        root.getConditions().add(SimpleCondition.isBlank(name(field)));
        return this;
    }

    /**
     * 不为null且不为空字符串 条件
     *
     * @param field 条件字段名
     */
    public ConditionBuilder isNotBlank(String field) {
        root.getConditions().add(SimpleCondition.isNotBlank(field));
        return this;
    }

    /**
     * 不为null且不为空字符串 条件
     *
     * @param field 条件字段名
     */
    public <T> ConditionBuilder isNotBlank(SFunction<T, ?> field) {
        root.getConditions().add(SimpleCondition.isNotBlank(name(field)));
        return this;
    }

    /**
     * in条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder in(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.in(field, value), false);
    }

    /**
     * in条件
     *
     * @param field 条件字段名
     * @param value 条件值，不能为null或空数组
     */
    public <T> ConditionBuilder in(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.in(name(field), value), false);
    }

    /**
     * 可选in条件，当value为null或空数组时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder inOptional(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.in(field, value), true);
    }

    /**
     * 可选in条件，当value为null或空数组时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder inOptional(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.in(name(field), value), true);
    }

    /**
     * notIn条件
     *
     * @param field 条件字段名
     * @param value 条件值，不能为null或空数组
     */
    public <T> ConditionBuilder notIn(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.notIn(field, value), false);
    }

    /**
     * notIn条件
     *
     * @param field 条件字段名
     * @param value 条件值，不能为null或空数组
     */
    public <T> ConditionBuilder notIn(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.notIn(name(field), value), false);
    }

    /**
     * 可选notIn条件，当value为null或空数组时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder notInOptional(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.notIn(field, value), true);
    }

    /**
     * 可选notIn条件，当value为null或空数组时将不加入该查询条件
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder notInOptional(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.notIn(name(field), value), true);
    }


    /**
     * 等于或者in条件；如果条件值列表只有一条数据，则使用等于条件，否则使用in条件。
     *
     * @param field 条件字段名
     * @param value 条件值，不能为null或空数组
     */
    public ConditionBuilder eqOrIn(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.eqOrIn(field, value), false);
    }

    /**
     * 等于或者in条件；如果条件值列表只有一条数据，则使用等于条件，否则使用in条件。
     *
     * @param field 条件字段名
     * @param value 条件值，不能为null或空数组
     */
    public <T> ConditionBuilder eqOrIn(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.eqOrIn(name(field), value), false);
    }

    /**
     * 可选等于或者in条件；如果条件值列表为null或空条件被忽略，如果只有一条数据则使用等于条件，否则使用in条件。
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public ConditionBuilder eqOrInOptional(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.eqOrIn(field, value), true);
    }

    /**
     * 可选等于或者in条件；如果条件值列表为null或空条件被忽略，如果只有一条数据则使用等于条件，否则使用in条件。
     *
     * @param field 条件字段名
     * @param value 条件值
     */
    public <T> ConditionBuilder eqOrInOptional(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.eqOrIn(name(field), value), true);
    }

    /**
     * 存在条件
     *
     * @param field           关联其他表的字段名，对应字段可以是ToOne 或 ToMany字段类型
     * @param existsCondition 存在条件
     */
    public ConditionBuilder exists(String field, Consumer<ConditionBuilder> existsCondition) {
        addInnerBuilder(existsCondition, b -> ExistsCondition.of(field, b.build()));
        return this;
    }

    public ConditionBuilder exists(String field, List<Join> joins, Consumer<ConditionBuilder> existsCondition) {
        addInnerBuilder(existsCondition, b -> ExistsCondition.of(field, b.build(), joins));
        return this;
    }

    /**
     * 存在条件
     *
     * @param field           关联其他表的字段名，对应字段可以是ToOne 或 ToMany字段类型
     * @param existsCondition 存在条件
     */
    public <T> ConditionBuilder exists(SFunction<T, ?> field, Consumer<ConditionBuilder> existsCondition) {
        addInnerBuilder(existsCondition, b -> ExistsCondition.of(name(field), b.build()));
        return this;
    }

    public <T> ConditionBuilder exists(SFunction<T, ?> field, List<Join> joins, Consumer<ConditionBuilder> existsCondition) {
        addInnerBuilder(existsCondition, b -> ExistsCondition.of(name(field), b.build(), joins));
        return this;
    }

    /**
     * 存在条件
     *
     * @param field      关联其他表的字段名，对应字段可以是ToOne 或 ToMany字段类型
     * @param conditions 存在条件，多条为And关系
     */
    public ConditionBuilder exists(String field, Condition... conditions) {
        root.getConditions().add(ExistsCondition.of(field, GroupCondition.and(conditions)));
        return this;
    }

    public ConditionBuilder exists(String field, List<Join> joins, Condition... conditions) {
        root.getConditions().add(ExistsCondition.of(field, GroupCondition.and(conditions), joins));
        return this;
    }

    /**
     * 存在条件
     *
     * @param field      关联其他表的字段名，对应字段可以是ToOne 或 ToMany字段类型
     * @param conditions 存在条件，多条为And关系
     */
    public <T> ConditionBuilder exists(SFunction<T, ?> field, Condition... conditions) {
        root.getConditions().add(ExistsCondition.of(name(field), GroupCondition.and(conditions)));
        return this;
    }

    public <T> ConditionBuilder exists(SFunction<T, ?> field, List<Join> joins, Condition... conditions) {
        root.getConditions().add(ExistsCondition.of(name(field), GroupCondition.and(conditions), joins));
        return this;
    }


    /**
     * 将示例bean 中 非空、非空字符串属性相同取值 作为查询条件
     */
    public ConditionBuilder eqSample(Object bean) {
        Map<String, Object> map = DataUtil.getNoEmptyValueMap(bean);
        map.forEach((k, v) -> {
            root.getConditions().add(SimpleCondition.eq(k, v));
        });
        return this;
    }
}
