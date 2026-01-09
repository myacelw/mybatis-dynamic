package io.github.myacelw.mybatis.dynamic.core.event.data;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import lombok.Getter;

/**
 * 逻辑删除模型数据事件
 *
 * @author liuwei
 */
@Getter
public class LogicDeleteDataEvent<ID> extends DataEvent {

    protected final ID id;

    public LogicDeleteDataEvent(Model model, ID id) {
        super(model);
        this.id = id;
    }

}
