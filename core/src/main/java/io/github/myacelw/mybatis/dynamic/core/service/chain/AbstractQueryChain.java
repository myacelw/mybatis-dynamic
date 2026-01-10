package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.ConditionBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.AbstractQueryCommand;
import lombok.NonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 抽象查询链
 *
 * @author liuwei
 */
public abstract class AbstractQueryChain<ID, T, R, C extends AbstractQueryCommand<T>, B extends AbstractQueryChain<ID, T, R, C, B>> extends AbstractChain<ID, R, C, B> {

    public AbstractQueryChain(DataManager<ID> dataManager, Supplier<C> createCommand) {
        super(dataManager, createCommand);
    }

    public AbstractQueryChain(DataManager<ID> dataManager, Supplier<C> createCommand, Class<T> clazz) {
        super(dataManager, createCommand);
        command.setClazz(clazz);
    }

    /**
     * 设置条件
     */
    public B where(Condition condition) {
        command.setCondition(condition);
        return self();
    }

    /**
     * 构造条件
     *
     * @param conditionBuilderConfig 条件构造器的配置函数
     */
    public B where(Consumer<ConditionBuilder> conditionBuilderConfig) {
        if (conditionBuilderConfig != null) {
            ConditionBuilder builder = Condition.builder();
            conditionBuilderConfig.accept(builder);
            command.setCondition(builder.build());
        }
        return self();
    }

    /**
     * 设置排序
     */
    public B orderItems(List<OrderItem> orderItems) {
        add(command::getOrderItems, command::setOrderItems, orderItems);
        return self();
    }

    public B orderItems(OrderItem... orderItems) {
        add(command::getOrderItems, command::setOrderItems, orderItems);
        return self();
    }

    /**
     * 添加排序
     */
    public B orderItem(@NonNull OrderItem orderItem) {
        add(command::getOrderItems, command::setOrderItems, orderItem);
        return self();
    }

    public B asc(@NonNull String field) {
        return orderItem(OrderItem.asc(field));
    }

    public B desc(@NonNull String field) {
        return orderItem(OrderItem.desc(field));
    }

    /**
     * 设置关联查询
     */
    public B joins(List<Join> joins) {
        add(command::getJoins, command::setJoins, joins);
        return self();
    }

    public B joins(Join... joins) {
        add(command::getJoins, command::setJoins, joins);
        return self();
    }

    /**
     * 关联查询涉及的字段及其对应模型、附加查询条件等；支持多级级联嵌套；
     * 可以对加入的ToOne 或 ToMany 类型字段 查询相关表数据。
     * 例如：查询User数据，可以级联查询 department 属性对应模型，和 department.company 对应模型。
     */
    public B join(@NonNull Join join) {
        add(command::getJoins, command::setJoins, join);
        return self();
    }

    /**
     * 是否忽略逻辑删除
     */
    public B ignoreLogicDelete() {
        command.setIgnoreLogicDelete(true);
        return self();
    }

}
