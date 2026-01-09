package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.PageResult;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.PageCommand;

/**
 * 分车查询处理链
 *
 * @author liuwei
 */
public class PageChain<ID, T> extends AbstractSelectQueryChain<ID, T, PageResult<T>, PageCommand<T>, PageChain<ID, T>> {

    public PageChain(DataManager<ID> dataManager) {
        super(dataManager, PageCommand::new);
    }

    public PageChain(DataManager<ID> dataManager, Class<T> clazz) {
        super(dataManager, PageCommand::new, clazz);
    }

    /**
     * 设置分页
     */
    public PageChain<ID, T> page(Page page) {
        command.setPage(page);
        return this;
    }

    public PageChain<ID, T> page(int current, int size) {
        command.setPage(new Page(current, size));
        return this;
    }


}
