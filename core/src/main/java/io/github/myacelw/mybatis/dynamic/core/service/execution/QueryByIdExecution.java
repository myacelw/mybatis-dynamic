package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.exception.crud.JoinException;
import io.github.myacelw.mybatis.dynamic.core.exception.data.DataNotFoundException;
import io.github.myacelw.mybatis.dynamic.core.exception.data.NonUniqueDataException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryByIdCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.IdUtil;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;

import java.util.List;

/**
 * 按ID查询一条数据执行器
 *
 * @author liuwei
 */
public class QueryByIdExecution<ID, T> extends AbstractExecution<ID, T, QueryByIdCommand<ID, T>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return QueryByIdCommand.class;
    }


    @Override
    public T exec(QueryByIdCommand<ID, T> command, DataManager<ID> dataManager) {
        Assert.notNull(command.getId(), "ID cannot be null");
        if (command.getJoins() != null) {
            if (command.getJoins().stream().anyMatch(t -> t.getType() == Join.JoinType.RIGHT || t.getType() == Join.JoinType.FULL)) {
                throw new JoinException("Model [" + dataManager.getModel().getName() + "] query by ID returns only one record, thus RIGHT or FULL JOIN is not supported");
            }
        }

        Model model = dataManager.getModel();

        List<T> list = dataManager.queryChain(command.getClazz())
                .where(IdUtil.getIdCondition(model, command.getId()))
                .page(ObjectUtil.isEmpty(command.getJoins()) ? new Page(1, 1) : null)
                .joins(command.getJoins())
                .select(command.getSelectFields())
                .exec();

        return getOne(list, command.isNullThrowException());
    }

    private T getOne(List<T> list, boolean nullThrowException) {
        if (list.isEmpty()) {
            if (nullThrowException) {
                throw new DataNotFoundException("Data with specified ID not found");
            }
            return null;
        }

        if (list.size() > 1) {
            throw new NonUniqueDataException("Data error: expected single record but multiple records were found");
        }

        return list.get(0);
    }


}
