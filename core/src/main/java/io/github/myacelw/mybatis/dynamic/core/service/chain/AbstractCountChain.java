package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.CountCommand;
import lombok.NonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 抽象的Count处理链
 *
 * @author liuwei
 */
public abstract class AbstractCountChain<ID, C extends CountCommand, B extends AbstractCountChain<ID, C, B>> extends AbstractChain<ID, Integer, C, B> {

    public AbstractCountChain(DataManager<ID> dataManager, Supplier<C> createCommand) {
        super(dataManager, createCommand);
    }

    /**
     * 设置条件
     */
    public B where(Condition condition) {
        command.setCondition(condition);
        return self();
    }

    public B where(Consumer<Condition.ConditionBuilder> conditionBuilderConfig) {
        if (conditionBuilderConfig != null) {
            Condition.ConditionBuilder builder = Condition.builder();
            conditionBuilderConfig.accept(builder);
            command.setCondition(builder.build());
        }
        return self();
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

}
