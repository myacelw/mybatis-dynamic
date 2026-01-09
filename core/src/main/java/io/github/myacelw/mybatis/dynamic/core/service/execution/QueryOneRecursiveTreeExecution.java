package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryOneRecursiveTreeCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryRecursiveTreeCommand;

import java.util.List;

/**
 * 查询一条树形结构数据执行器
 *
 * @author liuwei
 */
public class QueryOneRecursiveTreeExecution<ID, T> extends AbstractExecution<ID, T, QueryOneRecursiveTreeCommand<T>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return QueryOneRecursiveTreeCommand.class;
    }

    @Override
    public T exec(QueryOneRecursiveTreeCommand<T> command, DataManager<ID> dataManager) {
        QueryRecursiveTreeCommand<T> queryCommand = new QueryRecursiveTreeCommand<>();
        queryCommand.copyProperties(command);
        queryCommand.setInitNodeCondition(command.getInitNodeCondition());

        List<T> list = dataManager.execCommand(queryCommand);

        return QueryOneExecution.getOne(list, command.isNullThrowException(), command.isOnlyFirst());
    }

}
