package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryRecursiveListCommand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 递归查询返回列表执行器
 *
 * @author liuwei
 */
public class QueryRecursiveListExecution<ID, T> extends AbstractExecution<ID, List<T>, QueryRecursiveListCommand<T>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return QueryRecursiveListCommand.class;
    }

    @Override
    public List<T> exec(QueryRecursiveListCommand<T> command, DataManager<ID> dataManager) {
        QueryCommand<T> queryCommand = new QueryCommand<>();
        queryCommand.copyProperties(command);
        queryCommand.setPage(command.getPage());

        List<String> recursiveFields = getRecursiveFields(command.getSelectFields(), command.getCondition(), command.getOrderItems(), command.getJoins());

        return QueryExecution.doQuery(
                queryCommand,
                dataManager.getModelContext(),
                root -> root.getSelectColumns(false, command.getClazz() != null, command.getCustomSelectFields()),
                command.getInitNodeCondition(),
                command.isRecursiveDown(),
                recursiveFields
        );
    }


    public static List<String> getRecursiveFields(List<String> sourceSelectFields, Condition condition, List<OrderItem> orderItems, List<Join> joins) {
        if (sourceSelectFields == null) {
            return null;
        }

        Set<String> set = new HashSet<>();
        for (String fieldPath : sourceSelectFields) {
            set.add(getFieldForRecursive(fieldPath));
        }
        if (condition != null) {
            for (String fieldPath : condition.innerGetSimpleConditionFields()) {
                set.add(getFieldForRecursive(fieldPath));
            }
            for (String fieldPath : condition.innerGetExistsConditionFields()) {
                set.add(getFieldForRecursive(fieldPath));
            }
        }
        if (orderItems != null) {
            for (OrderItem orderItem : orderItems) {
                if (orderItem.getField() != null) {
                    set.add(getFieldForRecursive(orderItem.getField()));
                }
            }
        }
        if (joins != null) {
            for (Join join : joins) {
                if (join.getFieldPath() != null) {
                    set.add(getFieldForRecursive(join.getFieldPath()));
                }
            }
        }
        return new ArrayList<>(set);
    }


    private static String getFieldForRecursive(String fieldPath) {
        if (fieldPath.contains(".")) {
            return fieldPath.substring(0, fieldPath.indexOf("."));
        }
        return fieldPath;
    }

}
