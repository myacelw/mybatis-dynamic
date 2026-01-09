package io.github.myacelw.mybatis.dynamic.core.event.model;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import lombok.Getter;

/**
 * 修改模型名事件
 *
 * @author liuwei
 */
@Getter
public class RenameModelEvent extends ModelEvent {

    private final String oldName;

    public RenameModelEvent(Model model, String oldName) {
        super(model);
        this.oldName = oldName;
    }

}
