package io.github.myacelw.mybatis.dynamic.core.event.data;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import lombok.Getter;

/**
 * 物理删除模型数据事件
 *
 * @author liuwei
 */
@Getter
public class PhysicalDeleteDataEvent<ID> extends DataEvent {

    protected final ID id;

    public PhysicalDeleteDataEvent(Model model, ID id) {
        super(model);
        this.id = id;
    }

}
