package io.github.myacelw.mybatis.dynamic.core.event.data;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import lombok.Getter;

/**
 * 更新模型数据事件
 *
 * @author liuwei
 */
@Getter
public class UpdateDataEvent<ID> extends DataEvent {

    protected final ID id;

    protected final Object data;

    public UpdateDataEvent(Model model, ID id, Object data) {
        super(model);
        this.id = id;
        this.data = data;
    }
}
