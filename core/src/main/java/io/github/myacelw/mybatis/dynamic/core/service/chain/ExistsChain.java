package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.ConditionBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.ExistsCommand;
import lombok.NonNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * 是否存在查询处理链
 *
 * @author liuwei
 */
public class ExistsChain<ID> extends AbstractChain<ID, Boolean, ExistsCommand, ExistsChain<ID>> {

    public ExistsChain(DataManager<ID> dataManager) {
        super(dataManager, ExistsCommand::new);
    }

    /**
     * 设置条件
     */
    public ExistsChain<ID> where(Condition condition) {
        command.setCondition(condition);
        return self();
    }

    public ExistsChain<ID> where(Consumer<ConditionBuilder> condition) {
        if (condition != null) {
            ConditionBuilder builder = Condition.builder();
            condition.accept(builder);
            command.setCondition(builder.build());
        }
        return self();
    }

    /**
     * 设置关联查询
     */
    public ExistsChain<ID> joins(List<Join> joins) {
        add(command::getJoins, command::setJoins, joins);
        return self();
    }

    public ExistsChain<ID> joins(Join... joins) {
        add(command::getJoins, command::setJoins, joins);
        return self();
    }

    /**
     * 关联查询涉及的字段及其对应模型、附加查询条件等；支持多级级联嵌套；
     * 可以对加入的ToOne 或 ToMany 类型字段 查询相关表数据。
     * 例如：查询User数据，可以级联查询 department 属性对应模型，和 department.company 对应模型。
     */
    public ExistsChain<ID> join(@NonNull Join join) {
        add(command::getJoins, command::setJoins, join);
        return self();
    }

}
