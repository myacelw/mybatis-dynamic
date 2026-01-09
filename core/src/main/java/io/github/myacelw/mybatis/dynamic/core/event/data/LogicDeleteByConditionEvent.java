package io.github.myacelw.mybatis.dynamic.core.event.data;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import lombok.Getter;

/**
 * 按条件逻辑删除模型数据事件
 *
 * @author liuwei
 */
@Getter
public class LogicDeleteByConditionEvent extends DataEvent {

    protected final Condition condition;

    public LogicDeleteByConditionEvent(Model model, Condition condition) {
        super(model);
        this.condition = condition;
    }

}
