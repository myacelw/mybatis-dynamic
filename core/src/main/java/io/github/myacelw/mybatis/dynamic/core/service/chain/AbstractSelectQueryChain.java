package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.CustomSelectField;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.SelectBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.command.AbstractSelectQueryCommand;
import io.github.myacelw.mybatis.dynamic.core.util.lambda.SFunction;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.myacelw.mybatis.dynamic.core.util.lambda.LambdaUtil.names;

/**
 * 抽象带有查询返回列设置的查询链
 *
 * @author liuwei
 */
public abstract class AbstractSelectQueryChain<ID, T, R, C extends AbstractSelectQueryCommand<T>, B extends AbstractSelectQueryChain<ID, T, R, C, B>> extends AbstractQueryChain<ID, T, R, C, B> {

    public AbstractSelectQueryChain(DataManager<ID> dataManager, Supplier<C> createCommand) {
        super(dataManager, createCommand);
    }

    public AbstractSelectQueryChain(DataManager<ID> dataManager, Supplier<C> createCommand, Class<T> clazz) {
        super(dataManager, createCommand);
        command.setClazz(clazz);
    }

    public B selectFieldsForId() {
        String[] primaryKeyFields = dataManager.getModel().getPrimaryKeyFields();
        return select(primaryKeyFields == null ? new String[0] : primaryKeyFields);
    }

    /**
     * 设置返回字段
     */
    public B select(Consumer<SelectBuilder> b) {
        SelectBuilder selectBuilder = new SelectBuilder(dataManager);
        b.accept(selectBuilder);
        add(command::getSelectFields, command::setSelectFields, selectBuilder.build());
        return self();
    }


    /**
     * 设置返回字段
     */
    public B select(List<String> selectFields) {
        add(command::getSelectFields, command::setSelectFields, selectFields);
        return self();
    }

    /**
     * 设置返回字段
     */
    public B select(String... selectFields) {
        add(command::getSelectFields, command::setSelectFields, selectFields);
        return self();
    }

    @SafeVarargs
    public final B select(SFunction<T, ?>... selectFields) {
        add(command::getSelectFields, command::setSelectFields, names(selectFields));
        return self();
    }

    public B select(CustomSelectField... customSelectFields) {
        add(command::getCustomSelectFields, command::setCustomSelectFields, customSelectFields);
        return self();
    }

}
