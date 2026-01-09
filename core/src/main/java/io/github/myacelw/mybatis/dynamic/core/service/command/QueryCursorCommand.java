package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 游标查询命令
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryCursorCommand<T> extends AbstractSelectQueryCommand<T> {

    /**
     * 分页，这里的行数是join子表、关系表后的整体行数
     */
    Page page;

    public static QueryCursorCommand<Map<String, Object>> build() {
        return new QueryCursorCommand<>();
    }


}
