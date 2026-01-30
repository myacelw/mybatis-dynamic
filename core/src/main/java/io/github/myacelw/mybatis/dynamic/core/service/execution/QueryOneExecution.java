package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.exception.data.DataNotFoundException;
import io.github.myacelw.mybatis.dynamic.core.exception.data.NonUniqueDataException;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryOneCommand;

import java.util.List;

/**
 * 查询一条数据执行器
 *
 * @author liuwei
 */
public class QueryOneExecution<ID, T> extends AbstractExecution<ID, T, QueryOneCommand<T>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return QueryOneCommand.class;
    }

    @Override
    public T exec(QueryOneCommand<T> command, DataManager<ID> dataManager) {
        QueryCommand<T> queryCommand = new QueryCommand<>();
        queryCommand.copyProperties(command);
        if (command.isOnlyFirst()) {
            queryCommand.setPage(new Page(1, 1));
        } else {
            queryCommand.setPage(new Page(1, 2));
        }
        List<T> list = dataManager.execCommand(queryCommand);

        return getOne(list, command.isNullThrowException(), command.isOnlyFirst());
    }

    public static <T> T getOne(List<T> list, boolean nullThrowException, boolean onlyFirst) {
        if (list.isEmpty()) {
            if (nullThrowException) {
                throw new DataNotFoundException("Data not found");
            }
            return null;
        }

        if (list.size() > 1) {
            if (onlyFirst) {
                return list.get(0);
            } else {
                throw new NonUniqueDataException("Non-unique data: multiple records found");
            }
        }

        return list.get(0);
    }

}
