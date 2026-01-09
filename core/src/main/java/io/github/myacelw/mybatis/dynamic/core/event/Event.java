package io.github.myacelw.mybatis.dynamic.core.event;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;

/**
 * 事件
 *
 * @author liuwei
 */
public interface Event {

    /**
     * 事件模型
     */
    Model getModel();

}
