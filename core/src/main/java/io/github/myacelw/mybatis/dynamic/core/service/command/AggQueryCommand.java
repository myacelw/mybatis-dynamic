package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.AggSelectItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 聚合查询命令
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AggQueryCommand<T> extends AbstractQueryCommand<T> {

    /**
     * 聚合查询返回的字段
     */
    List<AggSelectItem> aggSelectItems;

    /**
     * 分页，这里的行数是join子表、关系表后的整体行数
     */
    Page page;

    public static AggQueryCommand<Map<String, Object>> build() {
        return new AggQueryCommand<>();
    }

}
