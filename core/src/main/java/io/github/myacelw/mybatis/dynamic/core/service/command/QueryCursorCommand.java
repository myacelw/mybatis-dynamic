package io.github.myacelw.mybatis.dynamic.core.service.command;

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
     * 最大返回行数
     */
    private Integer limit;

     /**
      * 偏移量
      */
    private Integer offset;

    public static QueryCursorCommand<Map<String, Object>> build() {
        return new QueryCursorCommand<>();
    }


}
