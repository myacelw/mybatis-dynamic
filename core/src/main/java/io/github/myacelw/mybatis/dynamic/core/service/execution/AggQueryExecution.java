package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.AggFunction;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.AggSelectItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.AggQueryCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCommand;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合查询执行器
 *
 * @author liuwei
 */
public class AggQueryExecution<ID, T> extends AbstractExecution<ID, List<T>, AggQueryCommand<T>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return AggQueryCommand.class;
    }

    @Override
    public List<T> exec(AggQueryCommand<T> command, DataManager<ID> dataManager) {
        Assert.notEmpty(command.getAggSelectItems(), "参数aggSelectItems不能为空");

        List<OrderItem> orderItems = new ArrayList<>();
        if (command.getOrderItems() != null) {
            orderItems.addAll(command.getOrderItems());
        }

        for (AggSelectItem item : command.getAggSelectItems()) {
            if (!StringUtil.hasText(item.getPropertyName()) && StringUtil.hasText(item.getField())) {
                if (item.getAggFunction() != null && item.getAggFunction() != AggFunction.NONE) {
                    item.setPropertyName(item.getField() + "_" + item.getAggFunction().name().toLowerCase());
                } else {
                    item.setPropertyName(item.getField());
                }
            }
            if (item.getOrderAsc() != null) {
                OrderItem orderItem = new OrderItem();
                orderItem.setField(item.getField());
                orderItem.setAsc(item.getOrderAsc());
                if (item.getAggFunction() == AggFunction.COUNT && ("1".equals(item.getField()) || "*".equals(item.getField()))) {
                    orderItem.setFunctionTemplate("COUNT(*)");
                } else if (item.getAggFunction() != null && item.getAggFunction() != AggFunction.NONE && item.getAggFunction() != AggFunction.CUSTOM) {
                    orderItem.setFunctionTemplate(item.getAggFunction().toSelectColumn("$COL", dataManager.getModelContext().getDialect()));
                } else {
                    orderItem.setFunctionTemplate(item.getCustomFunction());
                }
                orderItems.add(orderItem);
            }

            command.setOrderItems(orderItems);

            item.check();
        }

        QueryCommand<T> queryCommand = new QueryCommand<>();
        queryCommand.copyProperties(command);
        queryCommand.setPage(command.getPage());

        List<T> result = QueryExecution.doQuery(queryCommand, dataManager.getModelContext(), root -> root.getAggSelectColumns(command.getAggSelectItems()));

        if (result.size() == 1 && result.get(0) == null) {
            return new ArrayList<>();
        }
        return result;
    }

}
