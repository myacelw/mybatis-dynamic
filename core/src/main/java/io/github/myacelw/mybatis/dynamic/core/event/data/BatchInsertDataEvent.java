package io.github.myacelw.mybatis.dynamic.core.event.data;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import lombok.Getter;

import java.util.List;

/**
 * 批量插入数据事件
 *
 * @author liuwei
 */
@Getter
public class BatchInsertDataEvent extends DataEvent {

    protected final List<?> dataList;

    public BatchInsertDataEvent(Model model, List<?> dataList) {
        super(model);
        this.dataList = dataList;
    }
}
