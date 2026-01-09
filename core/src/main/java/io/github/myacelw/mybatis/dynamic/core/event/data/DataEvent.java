package io.github.myacelw.mybatis.dynamic.core.event.data;

import io.github.myacelw.mybatis.dynamic.core.event.Event;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import lombok.Getter;

/**
 * 数据相关事件
 *
 * @author liuwei
 */
public abstract class DataEvent implements Event {
    @Getter
    private final Model model;

    public DataEvent(Model source) {
        this.model = source;
    }
}
