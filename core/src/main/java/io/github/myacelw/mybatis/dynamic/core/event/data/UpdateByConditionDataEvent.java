package io.github.myacelw.mybatis.dynamic.core.event.data;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import lombok.Getter;

/**
 * 有条件批量更新模型数据事件
 *
 * @author liuwei
 */
@Getter
public class UpdateByConditionDataEvent extends DataEvent {

    protected final Condition condition;

    protected final Object data;

    public UpdateByConditionDataEvent(Model model, Condition condition, Object data) {
        super(model);
        this.condition = condition;
        this.data = data;
    }
}
