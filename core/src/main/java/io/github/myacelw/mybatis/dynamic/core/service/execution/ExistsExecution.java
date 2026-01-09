package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.CustomSelectField;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.ExistsCommand;

import java.util.Collections;
import java.util.List;

/**
 * 查询是否存在数据命令执行器
 *
 * @author liuwei
 */
public class ExistsExecution<ID> extends AbstractExecution<ID, Boolean, ExistsCommand> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return ExistsCommand.class;
    }

    @Override
    public Boolean exec(ExistsCommand command, DataManager<ID> dataManager) {
        CustomSelectField field = new CustomSelectField();
        field.setSqlTemplate("1");
        field.setJavaType(Integer.class);

        List<?> list = dataManager.queryChain()
                .select(field)
                .select(Collections.emptyList())
                .where(command.getCondition())
                .joins(command.getJoins())
                .limit(1)
                .exec();

        return !list.isEmpty();
    }
}
