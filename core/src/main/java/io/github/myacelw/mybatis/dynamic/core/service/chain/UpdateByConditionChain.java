package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.ConditionBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.UpdateByConditionCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.UpdateCommand;

import java.util.function.Consumer;

/**
 * 按条件更新数据处理链
 *
 * @author liuwei
 */
public class UpdateByConditionChain<ID> extends AbstractChain<ID, Integer, UpdateByConditionCommand, UpdateByConditionChain<ID>> {

    public UpdateByConditionChain(DataManager<ID> dataManager) {
        super(dataManager, UpdateByConditionCommand::new);
    }

    /**
     * 设置条件
     */
    public UpdateByConditionChain<ID> where(Condition condition) {
        command.setCondition(condition);
        return self();
    }

    public UpdateByConditionChain<ID> where(Consumer<ConditionBuilder> condition) {
        if (condition != null) {
            ConditionBuilder builder = Condition.builder();
            condition.accept(builder);
            command.setCondition(builder.build());
        }
        return self();
    }

    /**
     * 设置要更新的数据，可以是Map 或 实体对象
     */
    public UpdateByConditionChain<ID> data(Object data) {
        command.setData(data);
        return self();
    }

    /**
     * 是否只更新非空字段，为true时null字段值被跳过不更新
     */
    public UpdateByConditionChain<ID> onlyUpdateNonNull(boolean onlyUpdateNonNull) {
        command.setOnlyUpdateNonNull(onlyUpdateNonNull);
        return self();
    }

    public UpdateByConditionChain<ID> customSet(UpdateCommand.CustomSet customSet) {
        add(command::getCustomSetList, command::setCustomSetList, customSet);
        return this;
    }

    public UpdateByConditionChain<ID> customSet(String updateField, String sqlTemplate, Object value) {
        UpdateCommand.CustomSet customSet = UpdateCommand.CustomSet.builder()
                .updateField(updateField)
                .sqlTemplate(sqlTemplate)
                .value(value)
                .build();
        add(command::getCustomSetList, command::setCustomSetList, customSet);
        return this;
    }

    public UpdateByConditionChain<ID> customSet(String updateField, String sqlTemplate, String field, Object value) {
        UpdateCommand.CustomSet customSet = UpdateCommand.CustomSet.builder()
                .updateField(updateField)
                .sqlTemplate(sqlTemplate)
                .fields(new String[]{field})
                .value(value)
                .build();
        add(command::getCustomSetList, command::setCustomSetList, customSet);
        return this;
    }

}
