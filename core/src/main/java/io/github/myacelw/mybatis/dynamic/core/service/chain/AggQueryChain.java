package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.AggFunction;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.AggSelectItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.AggQueryCommand;
import lombok.NonNull;

import java.util.List;

/**
 * 聚合查询处理链
 *
 * @author liuwei
 */
public class AggQueryChain<ID, T> extends AbstractQueryChain<ID, T, List<T>, AggQueryCommand<T>, AggQueryChain<ID, T>> {

    public AggQueryChain(DataManager<ID> dataManager) {
        super(dataManager, AggQueryCommand::new);
    }

    public AggQueryChain(DataManager<ID> dataManager, Class<T> clazz) {
        super(dataManager, AggQueryCommand::new, clazz);
    }

    public AggQueryChain<ID, T> groupBy(String fieldPath, String propertyName) {
        AggSelectItem aggSelectItem = AggSelectItem.of(fieldPath, propertyName);
        add(command::getAggSelectItems, command::setAggSelectItems, aggSelectItem);
        return this;
    }

    public AggQueryChain<ID, T> groupBy(String fieldPath) {
        AggSelectItem aggSelectItem = AggSelectItem.of(fieldPath);
        add(command::getAggSelectItems, command::setAggSelectItems, aggSelectItem);
        return this;
    }

    public AggQueryChain<ID, T> sum(String fieldPath) {
        return aggSelectItem(fieldPath, AggFunction.SUM);
    }

    public AggQueryChain<ID, T> sum(String fieldPath, String propertyName) {
        return aggSelectItem(fieldPath, AggFunction.SUM, propertyName);
    }

    public AggQueryChain<ID, T> count() {
        return aggSelectItem(AggSelectItem.COUNT);
    }

    public AggQueryChain<ID, T> count(String fieldPath, String propertyName) {
        return aggSelectItem(fieldPath, AggFunction.COUNT, propertyName);
    }

    public AggQueryChain<ID, T> distinctCount(String fieldPath, String propertyName) {
        return aggSelectItem(fieldPath, AggFunction.COUNT_DISTINCT, propertyName);
    }

    public AggQueryChain<ID, T> avg(String fieldPath, String propertyName) {
        return aggSelectItem(fieldPath, AggFunction.AVG, propertyName);
    }

    public AggQueryChain<ID, T> max(String fieldPath, String propertyName) {
        return aggSelectItem(fieldPath, AggFunction.MAX, propertyName);
    }

    public AggQueryChain<ID, T> min(String fieldPath, String propertyName) {
        return aggSelectItem(fieldPath, AggFunction.MIN, propertyName);
    }

    public AggQueryChain<ID, T> aggSelectItem(@NonNull AggSelectItem aggSelectItem) {
        add(command::getAggSelectItems, command::setAggSelectItems, aggSelectItem);
        return this;
    }

    public AggQueryChain<ID, T> aggSelectItem(@NonNull String field, AggFunction aggFunction) {
        add(command::getAggSelectItems, command::setAggSelectItems, AggSelectItem.of(field, aggFunction));
        return this;
    }

    public AggQueryChain<ID, T> aggSelectItem(@NonNull String field, AggFunction aggFunction, String propertyName) {
        add(command::getAggSelectItems, command::setAggSelectItems, AggSelectItem.of(field, aggFunction, propertyName));
        return this;
    }

    /**
     * 设置分页
     */
    public AggQueryChain<ID, T> page(Page page) {
        command.setPage(page);
        return this;
    }

    public AggQueryChain<ID, T> page(int current, int size) {
        command.setPage(new Page(current, size));
        return this;
    }

    public AggQueryChain<ID, T> limit(int size) {
        command.setPage(new Page(1, size));
        return this;
    }


}
