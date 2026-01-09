package io.github.myacelw.mybatis.dynamic.core.event.data;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import lombok.Getter;

import java.util.List;

/**
 * 批量更新数据事件
 *
 * @author liuwei
 */
@Getter
public class BatchUpdateDataEvent extends DataEvent {

    protected final List<?> dataList;

    public BatchUpdateDataEvent(Model model, List<?> dataList) {
        super(model);
        this.dataList = dataList;
    }
}
