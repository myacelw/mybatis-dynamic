package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.AggSelectItem;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.CountRecursiveCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.myacelw.mybatis.dynamic.core.service.execution.QueryRecursiveListExecution.getRecursiveFields;

/**
 * 递归查询返回列表总条数执行器
 *
 * @author liuwei
 */
public class CountRecursiveExecution<ID> extends AbstractExecution<ID, Integer, CountRecursiveCommand> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return CountRecursiveCommand.class;
    }

    @Override
    public Integer exec(CountRecursiveCommand command, DataManager<ID> dataManager) {
        QueryCommand<Map<String, Object>> queryCommand = QueryCommand.build();
        queryCommand.setCondition(command.getCondition());
        queryCommand.setJoins(command.getJoins());

        List<String> recursiveFields = getRecursiveFields(new ArrayList<>(), command.getCondition(), null, command.getJoins());

        List<Map<String, Object>> list = QueryExecution.doQuery(
                queryCommand,
                dataManager.getModelContext(),
                root -> root.getAggSelectColumns(Collections.singletonList(AggSelectItem.COUNT)),
                command.getInitNodeCondition(),
                command.isRecursiveDown(),
                recursiveFields
        );

        Object result = list.get(0).get(AggSelectItem.COUNT.getPropertyName());
        if (result instanceof Long) {
            return ((Long) result).intValue();
        } else if (result instanceof Integer) {
            return (Integer) result;
        } else {
            throw new RuntimeException("Query result is not of type Long or Integer");
        }
    }
}
