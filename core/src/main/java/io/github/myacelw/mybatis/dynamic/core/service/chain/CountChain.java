package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.CountCommand;

/**
 * Count处理链
 *
 * @author liuwei
 */
public class CountChain<ID> extends AbstractCountChain<ID, CountCommand, CountChain<ID>> {

    public CountChain(DataManager<ID> dataManager) {
        super(dataManager, CountCommand::new);
    }

}
