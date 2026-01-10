package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.util.DataUtil;
import io.github.myacelw.mybatis.dynamic.core.util.lambda.SFunction;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition.Logic.AND;
import static io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition.Logic.OR;
import static io.github.myacelw.mybatis.dynamic.core.util.lambda.LambdaUtil.name;

/**
 * 条件构造器
 * 提供流式 API 构造复杂的查询条件，支持逻辑优先级处理。
 */
public class ConditionBuilder extends BaseBuilder<ConditionBuilder> {

    private GroupCondition root = new GroupCondition();

    public ConditionBuilder() {
        super(AND);
    }

    /**
     * 构建最终的 GroupCondition 对象
     */
    public GroupCondition build() {
        return this.root;
    }

    /**
     * 返回 AND 连接器
     */
    public AndConnector and() {
        return new AndConnector(this);
    }

    /**
     * 返回 OR 连接器
     */
    public OrConnector or() {
        return new OrConnector(this);
    }

    /**
     * 返回 NOT 连接器
     */
    public NotConnector not() {
        return new NotConnector(this, AND);
    }

    /**
     * 嵌套 AND 条件组
     */
    @Override
    public ConditionBuilder and(Consumer<ConditionBuilder> consumer) {
        return addInnerBuilder(consumer, ConditionBuilder::build, AND);
    }

    /**
     * 嵌套 OR 条件组
     */
    @Override
    public ConditionBuilder or(Consumer<ConditionBuilder> consumer) {
        return addInnerBuilder(consumer, b -> {
            b.root.setLogic(OR);
            return b.build();
        }, OR);
    }

    /**
     * 嵌套 NOT 条件组
     */
    @Override
    public ConditionBuilder not(Consumer<ConditionBuilder> consumer) {
        return addInnerBuilder(consumer, b -> NotCondition.of(b.build()), AND);
    }

    @Override
    protected ConditionBuilder add(Condition condition, GroupCondition.Logic logic) {
        if (root.getConditions().isEmpty()) {
            root.getConditions().add(condition);
            return this;
        }

        GroupCondition.Logic currentLogic = root.getLogic();

        if (logic == currentLogic) {
            root.getConditions().add(condition);
        } else if (logic == OR && currentLogic == AND) {
            if (root.getConditions().size() <= 1) {
                root.setLogic(OR);
                root.getConditions().add(condition);
            } else {
                GroupCondition oldRoot = this.root;
                this.root = new GroupCondition();
                this.root.setLogic(OR);
                this.root.getConditions().add(oldRoot);
                this.root.getConditions().add(condition);
            }
        } else if (logic == AND && currentLogic == OR) {
            Condition last = root.getConditions().remove(root.getConditions().size() - 1);
            GroupCondition subGroup = new GroupCondition();
            subGroup.setLogic(AND);
            subGroup.getConditions().add(last);
            subGroup.getConditions().add(condition);
            root.getConditions().add(subGroup);
        }
        return this;
    }

    @Override
    protected ConditionBuilder addInnerBuilder(Consumer<ConditionBuilder> c, Function<ConditionBuilder, Condition> f, GroupCondition.Logic logic) {
        ConditionBuilder b = Condition.builder();
        c.accept(b);
        Condition condition = f.apply(b);
        add(condition, logic);
        return this;
    }

    public ConditionBuilder eqSample(Object bean) {
        Map<String, Object> map = DataUtil.getNoEmptyValueMap(bean);
        map.forEach((k, v) -> {
            addSimpleCondition(SimpleCondition.eq(k, v), false);
        });
        return this;
    }

    // ================== Inner Classes ==================

    public static class Connector extends BaseBuilder<ConditionBuilder> {
        protected final ConditionBuilder cb;

        public Connector(ConditionBuilder cb, GroupCondition.Logic logic) {
            super(logic);
            this.cb = cb;
        }

        @Override
        protected ConditionBuilder add(Condition condition, GroupCondition.Logic logic) {
            return cb.add(condition, logic);
        }

        @Override
        protected ConditionBuilder addInnerBuilder(Consumer<ConditionBuilder> c, Function<ConditionBuilder, Condition> f, GroupCondition.Logic logic) {
            return cb.addInnerBuilder(c, f, logic);
        }

        @Override
        public ConditionBuilder and(Consumer<ConditionBuilder> consumer) {
            return cb.and(consumer);
        }

        @Override
        public ConditionBuilder or(Consumer<ConditionBuilder> consumer) {
            return cb.or(consumer);
        }

        @Override
        public ConditionBuilder not(Consumer<ConditionBuilder> consumer) {
            return cb.not(consumer);
        }
    }

    public static class AndConnector extends Connector {
        public AndConnector(ConditionBuilder cb) {
            super(cb, AND);
        }

        public NotConnector not() {
            return new NotConnector(cb, AND);
        }
    }

    public static class OrConnector extends Connector {
        public OrConnector(ConditionBuilder cb) {
            super(cb, OR);
        }

        public NotConnector not() {
            return new NotConnector(cb, OR);
        }
    }

    public static class NotConnector extends BaseBuilder<ConditionBuilder> {
        private final ConditionBuilder cb;

        public NotConnector(ConditionBuilder cb, GroupCondition.Logic logic) {
            super(logic);
            this.cb = cb;
        }

        @Override
        protected ConditionBuilder add(Condition c, GroupCondition.Logic logic) {
            return cb.add(NotCondition.of(c), logic);
        }

        @Override
        public ConditionBuilder bracket(Consumer<ConditionBuilder> consumer) {
            return cb.addInnerBuilder(consumer, b -> NotCondition.of(b.build()), logic);
        }
        
        @Override
        protected ConditionBuilder addInnerBuilder(Consumer<ConditionBuilder> c, Function<ConditionBuilder, Condition> f, GroupCondition.Logic logic) {
             return cb.addInnerBuilder(c, b -> NotCondition.of(f.apply(b)), logic);
        }

        @Override
        public ConditionBuilder and(Consumer<ConditionBuilder> consumer) {
            return cb.addInnerBuilder(consumer, b -> NotCondition.of(b.build()), logic);
        }

        @Override
        public ConditionBuilder or(Consumer<ConditionBuilder> consumer) {
            return cb.addInnerBuilder(consumer, b -> {
                b.root.setLogic(OR);
                return NotCondition.of(b.build());
            }, logic);
        }

        @Override
        public ConditionBuilder not(Consumer<ConditionBuilder> consumer) {
            // NOT(NOT(...)) => ...
            return cb.addInnerBuilder(consumer, ConditionBuilder::build, logic);
        }
    }
}

/**
 * 基础构造器，定义通用的条件构造方法
 * @param <R> 返回的构造器类型，用于链式调用
 */
abstract class BaseBuilder<R> {
    protected final GroupCondition.Logic logic;

    protected BaseBuilder(GroupCondition.Logic logic) {
        this.logic = logic;
    }

    protected abstract R add(Condition condition, GroupCondition.Logic logic);

    protected abstract R addInnerBuilder(Consumer<ConditionBuilder> c, Function<ConditionBuilder, Condition> f, GroupCondition.Logic logic);

    public abstract R and(Consumer<ConditionBuilder> consumer);
    public abstract R or(Consumer<ConditionBuilder> consumer);
    public abstract R not(Consumer<ConditionBuilder> consumer);

    /**
     * 显式括号分组
     */
    public R bracket(Consumer<ConditionBuilder> consumer) {
        return addInnerBuilder(consumer, ConditionBuilder::build, logic);
    }

    protected R addSimpleCondition(SimpleCondition c, boolean optional) {
        c.setIgnoreIfValueEmpty(optional);
        return add(c, logic);
    }

    /**
     * 等于条件
     */
    public R eq(String field, Object value) {
        return addSimpleCondition(SimpleCondition.eq(field, value), false);
    }

    /**
     * Lambda 表达式形式的等于条件
     */
    public <T> R eq(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.eq(name(field), value), false);
    }

    /**
     * 可选等于条件
     */
    public R eqOptional(String field, Object value) {
        return addSimpleCondition(SimpleCondition.eq(field, value), true);
    }

    /**
     * Lambda 表达式形式的可选等于条件
     */
    public <T> R eqOptional(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.eq(name(field), value), true);
    }

    /**
     * 小于条件
     */
    public R lt(String field, Object value) {
        return addSimpleCondition(SimpleCondition.lt(field, value), false);
    }

    /**
     * Lambda 表达式形式的小于条件
     */
    public <T> R lt(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.lt(name(field), value), false);
    }

    /**
     * 大于条件
     */
    public R gt(String field, Object value) {
        return addSimpleCondition(SimpleCondition.gt(field, value), false);
    }

    /**
     * Lambda 表达式形式的大于条件
     */
    public <T> R gt(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.gt(name(field), value), false);
    }

    /**
     * 不等于条件
     */
    public R ne(String field, Object value) {
        return addSimpleCondition(SimpleCondition.ne(field, value), false);
    }

    /**
     * Lambda 表达式形式的不等于条件
     */
    public <T> R ne(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.ne(name(field), value), false);
    }

    /**
     * 可选不等于条件
     */
    public R neOptional(String field, Object value) {
        return addSimpleCondition(SimpleCondition.ne(field, value), true);
    }

    /**
     * Lambda 表达式形式的可选不等于条件
     */
    public <T> R neOptional(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.ne(name(field), value), true);
    }

    /**
     * 大于等于条件
     */
    public R gte(String field, Object value) {
        return addSimpleCondition(SimpleCondition.gte(field, value), false);
    }

    /**
     * Lambda 表达式形式的大于等于条件
     */
    public <T> R gte(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.gte(name(field), value), false);
    }

    /**
     * 可选大于等于条件
     */
    public R gteOptional(String field, Object value) {
        return addSimpleCondition(SimpleCondition.gte(field, value), true);
    }

    /**
     * Lambda 表达式形式的可选大于等于条件
     */
    public <T> R gteOptional(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.gte(name(field), value), true);
    }

    /**
     * 小于等于条件
     */
    public R lte(String field, Object value) {
        return addSimpleCondition(SimpleCondition.lte(field, value), false);
    }

    /**
     * Lambda 表达式形式的小于等于条件
     */
    public <T> R lte(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.lte(name(field), value), false);
    }

    /**
     * 可选小于等于条件
     */
    public R lteOptional(String field, Object value) {
        return addSimpleCondition(SimpleCondition.lte(field, value), true);
    }

    /**
     * Lambda 表达式形式的可选小于等于条件
     */
    public <T> R lteOptional(SFunction<T, ?> field, Object value) {
        return addSimpleCondition(SimpleCondition.lte(name(field), value), true);
    }

    /**
     * Like 条件
     */
    public R like(String field, String value) {
        return addSimpleCondition(SimpleCondition.like(field, value), false);
    }

    /**
     * Lambda 表达式形式的 Like 条件
     */
    public <T> R like(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.like(name(field), value), false);
    }

    /**
     * 可选 Like 条件
     */
    public R likeOptional(String field, String value) {
        return addSimpleCondition(SimpleCondition.like(field, value), true);
    }

    /**
     * Lambda 表达式形式的可选 Like 条件
     */
    public <T> R likeOptional(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.like(name(field), value), true);
    }

    /**
     * 包含条件 (Like %value%)
     */
    public R contains(String field, String value) {
        return addSimpleCondition(SimpleCondition.contains(field, value), false);
    }

    /**
     * Lambda 表达式形式的包含条件
     */
    public <T> R contains(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.contains(name(field), value), false);
    }

    /**
     * 可选包含条件
     */
    public R containsOptional(String field, String value) {
        return addSimpleCondition(SimpleCondition.contains(field, value), true);
    }

    /**
     * Lambda 表达式形式的可选包含条件
     */
    public <T> R containsOptional(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.contains(name(field), value), true);
    }

    /**
     * 前缀条件 (Like value%)
     */
    public R startsWith(String field, String value) {
        return addSimpleCondition(SimpleCondition.startsWith(field, value), false);
    }

    /**
     * Lambda 表达式形式的前缀条件
     */
    public <T> R startsWith(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.startsWith(name(field), value), false);
    }

    /**
     * 可选前缀条件
     */
    public R startsWithOptional(String field, String value) {
        return addSimpleCondition(SimpleCondition.startsWith(field, value), true);
    }

    /**
     * Lambda 表达式形式的可选前缀条件
     */
    public <T> R startsWithOptional(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.startsWith(name(field), value), true);
    }

    /**
     * 后缀条件 (Like %value)
     */
    public R endsWith(String field, String value) {
        return addSimpleCondition(SimpleCondition.endsWith(field, value), false);
    }

    /**
     * Lambda 表达式形式的后缀条件
     */
    public <T> R endsWith(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.endsWith(name(field), value), false);
    }

    /**
     * 可选后缀条件
     */
    public R endsWithOptional(String field, String value) {
        return addSimpleCondition(SimpleCondition.endsWith(field, value), true);
    }

    /**
     * Lambda 表达式形式的可选后缀条件
     */
    public <T> R endsWithOptional(SFunction<T, ?> field, String value) {
        return addSimpleCondition(SimpleCondition.endsWith(name(field), value), true);
    }

    /**
     * IS NULL 条件
     */
    public R isNull(String field) {
        return add(SimpleCondition.isNull(field), logic);
    }

    /**
     * Lambda 表达式形式的 IS NULL 条件
     */
    public <T> R isNull(SFunction<T, ?> field) {
        return add(SimpleCondition.isNull(name(field)), logic);
    }

    /**
     * IS NOT NULL 条件
     */
    public R isNotNull(String field) {
        return add(SimpleCondition.isNotNull(field), logic);
    }

    /**
     * Lambda 表达式形式的 IS NOT NULL 条件
     */
    public <T> R isNotNull(SFunction<T, ?> field) {
        return add(SimpleCondition.isNotNull(name(field)), logic);
    }

    /**
     * IS BLANK 条件
     */
    public R isBlank(String field) {
        return add(SimpleCondition.isBlank(field), logic);
    }

    /**
     * Lambda 表达式形式的 IS BLANK 条件
     */
    public <T> R isBlank(SFunction<T, ?> field) {
        return add(SimpleCondition.isBlank(name(field)), logic);
    }

    /**
     * IS NOT BLANK 条件
     */
    public R isNotBlank(String field) {
        return add(SimpleCondition.isNotBlank(field), logic);
    }

    /**
     * Lambda 表达式形式的 IS NOT BLANK 条件
     */
    public <T> R isNotBlank(SFunction<T, ?> field) {
        return add(SimpleCondition.isNotBlank(name(field)), logic);
    }

    /**
     * Lambda 表达式形式的 IN 条件
     */
    public <T> R in(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.in(name(field), value), false);
    }

    /**
     * 可选 IN 条件
     */
    public R inOptional(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.in(field, value), true);
    }

    /**
     * Lambda 表达式形式的可选 IN 条件
     */
    public <T> R inOptional(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.in(name(field), value), true);
    }

    /**
     * NOT IN 条件
     */
    public R notIn(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.notIn(field, value), false);
    }

    /**
     * Lambda 表达式形式的 NOT IN 条件
     */
    public <T> R notIn(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.notIn(name(field), value), false);
    }

    /**
     * 可选 NOT IN 条件
     */
    public R notInOptional(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.notIn(field, value), true);
    }

    /**
     * Lambda 表达式形式的可选 NOT IN 条件
     */
    public <T> R notInOptional(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.notIn(name(field), value), true);
    }

    /**
     * 等于或 IN 条件
     */
    public R eqOrIn(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.eqOrIn(field, value), false);
    }

    /**
     * Lambda 表达式形式的等于或 IN 条件
     */
    public <T> R eqOrIn(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.eqOrIn(name(field), value), false);
    }

    /**
     * 可选等于或 IN 条件
     */
    public R eqOrInOptional(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.eqOrIn(field, value), true);
    }

    /**
     * Lambda 表达式形式的可选等于或 IN 条件
     */
    public <T> R eqOrInOptional(SFunction<T, ?> field, List<?> value) {
        return addSimpleCondition(SimpleCondition.eqOrIn(name(field), value), true);
    }

    /**
     * IN 条件
     */
    public R in(String field, List<?> value) {
        return addSimpleCondition(SimpleCondition.in(field, value), false);
    }

    /**
     * 存在条件
     */
    public R exists(String field, Consumer<ConditionBuilder> existsCondition) {
        return addInnerBuilder(existsCondition, b -> ExistsCondition.of(field, b.build()), logic);
    }

    /**
     * 存在条件
     */
    public R exists(String field, List<Join> joins, Consumer<ConditionBuilder> existsCondition) {
        return addInnerBuilder(existsCondition, b -> ExistsCondition.of(field, b.build(), joins), logic);
    }

    /**
     * Lambda 表达式形式的存在条件
     */
    public <T> R exists(SFunction<T, ?> field, Consumer<ConditionBuilder> existsCondition) {
        return addInnerBuilder(existsCondition, b -> ExistsCondition.of(name(field), b.build()), logic);
    }

    /**
     * Lambda 表达式形式的存在条件，支持 Join
     */
    public <T> R exists(SFunction<T, ?> field, List<Join> joins, Consumer<ConditionBuilder> existsCondition) {
        return addInnerBuilder(existsCondition, b -> ExistsCondition.of(name(field), b.build(), joins), logic);
    }
    
    /**
     * 存在条件 (数组形式)
     */
    public R exists(String field, Condition... conditions) {
        return add(ExistsCondition.of(field, GroupCondition.and(conditions)), logic);
    }

    /**
     * 存在条件 (数组形式)，支持 Join
     */
    public R exists(String field, List<Join> joins, Condition... conditions) {
        return add(ExistsCondition.of(field, GroupCondition.and(conditions), joins), logic);
    }

    /**
     * Lambda 表达式形式的存在条件 (数组形式)
     */
    public <T> R exists(SFunction<T, ?> field, Condition... conditions) {
        return add(ExistsCondition.of(name(field), GroupCondition.and(conditions)), logic);
    }

    /**
     * Lambda 表达式形式的存在条件 (数组形式)，支持 Join
     */
    public <T> R exists(SFunction<T, ?> field, List<Join> joins, Condition... conditions) {
        return add(ExistsCondition.of(name(field), GroupCondition.and(conditions), joins), logic);
    }
}
