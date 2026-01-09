package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryByIdsCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.IdUtil;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;

import java.util.List;

/**
 * 按ID查询一条数据执行器
 *
 * @author liuwei
 */
public class QueryByIdsExecution<ID, T> extends AbstractExecution<ID, List<T>, QueryByIdsCommand<ID, T>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return QueryByIdsCommand.class;
    }


    @Override
    public List<T> exec(QueryByIdsCommand<ID, T> command, DataManager<ID> dataManager) {
        Assert.notEmpty(command.getIds(), "ID不能为空");
        Model model = dataManager.getModel();

        return dataManager.queryChain(command.getClazz())
                .where(IdUtil.getIdsCondition(model, command.getIds()))
                .page(ObjectUtil.isEmpty(command.getJoins()) ? new Page(1, 1) : null)
                .joins(command.getJoins())
                .select(command.getSelectFields())
                .exec();
    }


}
