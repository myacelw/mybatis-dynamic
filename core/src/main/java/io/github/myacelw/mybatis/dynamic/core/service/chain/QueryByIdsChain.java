package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryByIdsCommand;
import io.github.myacelw.mybatis.dynamic.core.util.lambda.SFunction;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;

import static io.github.myacelw.mybatis.dynamic.core.util.lambda.LambdaUtil.names;

/**
 * 按ID查询数据处理链
 *
 * @author liuwei
 */
public class QueryByIdsChain<ID, T> extends AbstractChain<ID, List<T>, QueryByIdsCommand<ID, T>, QueryByIdsChain<ID, T>> {

    public QueryByIdsChain(DataManager<ID> dataManager) {
        super(dataManager, QueryByIdsCommand::new);
    }

    public QueryByIdsChain(DataManager<ID> dataManager, Class<T> clazz) {
        super(dataManager, QueryByIdsCommand::new);
        command.setClazz(clazz);
    }

    /**
     * 查询ID
     */
    public QueryByIdsChain<ID, T> ids(Collection<ID> ids) {
        command.setIds(ids);
        return this;
    }

    /**
     * 设置返回字段
     */
    public QueryByIdsChain<ID, T> selectFields(List<String> selectFields) {
        add(command::getSelectFields, command::setSelectFields, selectFields);
        return this;
    }

    /**
     * 设置返回字段
     */
    public QueryByIdsChain<ID, T> selectFields(String... selectFields) {
        add(command::getSelectFields, command::setSelectFields, selectFields);
        return this;
    }

    /**
     * 添加返回字段
     */
    public QueryByIdsChain<ID, T> selectField(@NonNull String selectField) {
        add(command::getSelectFields, command::setSelectFields, selectField);
        return this;
    }

    @SafeVarargs
    public final QueryByIdsChain<ID, T> selectFields(SFunction<T, ?>... selectFields) {
        add(command::getSelectFields, command::setSelectFields, names(selectFields));
        return self();
    }

    /**
     * 设置关联查询
     */
    public QueryByIdsChain<ID, T> joins(List<Join> joins) {
        add(command::getJoins, command::setJoins, joins);
        return this;
    }

    public QueryByIdsChain<ID, T> joins(Join... joins) {
        add(command::getJoins, command::setJoins, joins);
        return this;
    }

    /**
     * 添加关联查询。
     * 关联查询涉及的字段及其对应模型、附加查询条件等；支持多级级联嵌套；
     * 可以对加入的ToOne 或 ToMany 类型字段 查询相关表数据。
     * 例如：查询User数据，可以级联查询 department 属性对应模型，和 department.company 对应模型。
     */
    public QueryByIdsChain<ID, T> join(@NonNull Join join) {
        add(command::getJoins, command::setJoins, join);
        return this;
    }

}
