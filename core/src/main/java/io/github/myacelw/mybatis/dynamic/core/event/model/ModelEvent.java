package io.github.myacelw.mybatis.dynamic.core.event.model;

import io.github.myacelw.mybatis.dynamic.core.event.Event;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import lombok.Getter;

/**
 * 模型相关事件
 *
 * @author liuwei
 */
public abstract class ModelEvent implements Event {
    @Getter
    protected final Model model;

    public ModelEvent(Model model) {
        this.model = model;
    }
}
