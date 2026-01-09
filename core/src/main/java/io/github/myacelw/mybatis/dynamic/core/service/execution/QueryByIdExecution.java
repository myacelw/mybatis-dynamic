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
        Assert.notNull(command.getId(), "ID不能为空");
        if (command.getJoins() != null) {
            if (command.getJoins().stream().anyMatch(t -> t.getType() == Join.JoinType.RIGHT || t.getType() == Join.JoinType.FULL)) {
                throw new JoinException("模型[" + dataManager.getModel().getName() + "]按ID查询数据只返回一条记录，因此不支持右关联和全关联");
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
                throw new DataNotFoundException("没有查找到对应ID的数据");
            }
            return null;
        }

        if (list.size() > 1) {
            throw new NonUniqueDataException("数据异常，要求只返回一条记录，但查询出多条记录");
        }

        return list.get(0);
    }


}
