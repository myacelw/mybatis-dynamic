package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 递归查询返回列表命令
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryRecursiveListCommand<T> extends QueryRecursiveTreeCommand<T> {
    /**
     * 分页，这里的行数是join子表、关系表后的整体行数
     */
    Page page;

    /**
     * 向上递归还是向下递归
     * true 为向下递归， false 为向上递归
     */
    boolean recursiveDown;

    public static QueryRecursiveListCommand<Map<String, Object>> build() {
        return new QueryRecursiveListCommand<>();
    }


}
