package io.github.myacelw.mybatis.dynamic.core.event.data;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import lombok.Getter;

/**
 * 插入数据事件
 *
 * @author liuwei
 */
@Getter
public class InsertDataEvent extends DataEvent {

    protected final Object data;

    public InsertDataEvent(Model model, Object data) {
        super(model);
        this.data = data;
    }

}
