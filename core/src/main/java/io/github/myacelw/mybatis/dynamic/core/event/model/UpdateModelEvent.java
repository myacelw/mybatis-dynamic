package io.github.myacelw.mybatis.dynamic.core.event.model;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;

/**
 * 创建或更新模型事件
 *
 * @author liuwei
 */
public class UpdateModelEvent extends ModelEvent {

    public UpdateModelEvent(Model model, Table table) {
        super(model);
    }

}
