package io.github.myacelw.mybatis.dynamic.core.event.data;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.service.command.BatchUpdateByConditionCommand;
import lombok.Getter;

import java.util.List;

/**
 * 按条件批量更新数据事件
 *
 * @author conductor
 */
@Getter
public class BatchUpdateByConditionDataEvent extends DataEvent {

    protected final List<BatchUpdateByConditionCommand.UpdatePair> updatePairs;

    public BatchUpdateByConditionDataEvent(Model model, List<BatchUpdateByConditionCommand.UpdatePair> updatePairs) {
        super(model);
        this.updatePairs = updatePairs;
    }
}
