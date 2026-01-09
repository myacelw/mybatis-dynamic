package io.github.myacelw.mybatis.dynamic.core.event;

import java.util.List;

/**
 * 事件监听器
 *
 * @author liuwei
 */
public interface EventListener {

    void onEvent(Event event);

    static void onEvent(List<EventListener> eventListeners, Event event) {
        if (eventListeners != null) {
            eventListeners.forEach(listener -> listener.onEvent(event));
        }
    }

}
