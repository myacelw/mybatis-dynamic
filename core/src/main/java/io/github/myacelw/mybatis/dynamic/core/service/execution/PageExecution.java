package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.PageResult;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.PageCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCommand;

import java.util.List;

/**
 * 分页查询执行器
 *
 * @author liuwei
 */
public class PageExecution<ID, T> extends AbstractExecution<ID, PageResult<T>, PageCommand<T>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return PageCommand.class;
    }

    @Override
    public PageResult<T> exec(PageCommand<T> command, DataManager<ID> dataManager) {
        Page page = getPage(command);

        QueryCommand<T> queryCommand = new QueryCommand<>();
        queryCommand.copyProperties(command);
        queryCommand.setPage(page);

        List<T> data = dataManager.execCommand(queryCommand);
        int count;
        if (page.getCurrent() == 1 && data.isEmpty()) {
            count = 0;
        } else if (data.size() < page.getSize()) {
            count = page.getSize() * (page.getCurrent() - 1) + data.size();
        } else {
            count = dataManager.countChain().where(command.getCondition()).joins(command.getJoins()).exec();
        }
        return new PageResult<>(data, count);
    }

    private Page getPage(PageCommand<T> command) {
        Page page = new Page();
        if (command.getPage() != null) {
            page.setSize(command.getPage().getSize());
            page.setCurrent(command.getPage().getCurrent());
        }
        if (page.getCurrent() <= 0) {
            page.setCurrent(1);
        }
        if (page.getSize() <= 0) {
            page.setSize(10);
        }
        return page;
    }

}
