package io.github.myacelw.mybatis.dynamic.core.event.model;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;

/**
 * 删除模型事件
 *
 * @author liuwei
 */
public class DeleteModelEvent extends ModelEvent {
    public DeleteModelEvent(Model model) {
        super(model);
    }

}
