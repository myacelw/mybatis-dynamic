package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryRecursiveListCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryRecursiveTreeCommand;
import io.github.myacelw.mybatis.dynamic.core.util.DataUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 递归查询返回树形结构执行器
 *
 * @author liuwei
 */
public class QueryRecursiveTreeExecution<ID, T> extends AbstractExecution<ID, List<T>, QueryRecursiveTreeCommand<T>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return QueryRecursiveTreeCommand.class;
    }

    @Override
    public List<T> exec(QueryRecursiveTreeCommand<T> command, DataManager<ID> dataManager) {
        Model model = dataManager.getModel();

        QueryRecursiveListCommand<T> queryCommand = new QueryRecursiveListCommand<>();
        queryCommand.copyProperties(command);
        queryCommand.setInitNodeCondition(command.getInitNodeCondition());
        queryCommand.setRecursiveDown(true);

        List<T> data = dataManager.execCommand(queryCommand);

        return DataUtil.buildTree(data, Arrays.stream(model.getParentIdFields()).map(BasicField::getName).toArray(String[]::new), model.getChildrenField(false).getName(), model.getPrimaryKeyFields());
    }

}
