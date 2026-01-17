package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.UpdateCommand;
import io.github.myacelw.mybatis.dynamic.core.util.lambda.SFunction;

import java.util.List;

import static io.github.myacelw.mybatis.dynamic.core.util.lambda.LambdaUtil.names;

/**
 * 更新数据处理链
 *
 * @author liuwei
 */
public class UpdateChain<ID> extends AbstractChain<ID, Void, UpdateCommand<ID>, UpdateChain<ID>> {

    public UpdateChain(DataManager<ID> dataManager) {
        super(dataManager, UpdateCommand::new);
    }

    public UpdateChain<ID> id(ID id) {
        command.setId(id);
        return this;
    }

    /**
     * 需要更新的数据
     */
    public UpdateChain<ID> data(Object data) {
        command.setData(data);
        return this;
    }

    /**
     * 是否强制更新，为true是不判断数据是否变化，这样修改时间等审计信息一定会变化
     */
    public UpdateChain<ID> force(boolean force) {
        command.setForce(force);
        return this;
    }

    public UpdateChain<ID> force() {
        command.setForce(true);
        return this;
    }

    /**
     * 是否只更新非空值，为true时null字段值被跳过不更新
     */
    public UpdateChain<ID> updateOnlyNonNull(boolean updateOnlyNonNull) {
        command.setUpdateOnlyNonNull(updateOnlyNonNull);
        return this;
    }

    public UpdateChain<ID> updateOnlyNonNull() {
        command.setUpdateOnlyNonNull(true);
        return this;
    }

    public UpdateChain<ID> updateFields(List<String> updateFields) {
        command.setUpdateFields(updateFields);
        return this;
    }

    public UpdateChain<ID> updateFields(String... updateFields) {
        add(command::getUpdateFields, command::setUpdateFields, updateFields);
        return this;
    }

    @SafeVarargs
    public final <T> UpdateChain<ID> updateFields(SFunction<T, ?>... updateFields) {
        add(command::getUpdateFields, command::setUpdateFields, names(updateFields));
        return this;
    }

    public UpdateChain<ID> ignoreFields(List<String> ignoreFields) {
        command.setIgnoreFields(ignoreFields);
        return this;
    }

    public UpdateChain<ID> ignoreFields(String... ignoreFields) {
        add(command::getIgnoreFields, command::setIgnoreFields, ignoreFields);
        return this;
    }

    @SafeVarargs
    public final <T> UpdateChain<ID> ignoreFields(SFunction<T, ?>... ignoreFields) {
        add(command::getIgnoreFields, command::setIgnoreFields, names(ignoreFields));
        return this;
    }

}
