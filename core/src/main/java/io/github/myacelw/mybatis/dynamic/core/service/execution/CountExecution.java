package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.AggSelectItem;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.CountCommand;

import java.util.List;

/**
 * Count查询执行器
 *
 * @author liuwei
 */
public class CountExecution<ID> extends AbstractExecution<ID, Integer, CountCommand> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return CountCommand.class;
    }

    @Override
    public Integer exec(CountCommand command, DataManager<ID> dataManager) {
        List<Integer> list = dataManager.aggQueryChain(Integer.class)
                .aggSelectItem(AggSelectItem.COUNT)
                .where(command.getCondition())
                .joins(command.getJoins()).exec();

        Integer count = list.get(0);

        return count == null ? 0 : count;
    }
}
