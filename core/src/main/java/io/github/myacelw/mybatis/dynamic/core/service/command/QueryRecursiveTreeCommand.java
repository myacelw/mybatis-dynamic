package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 递归查询返回树形结构命令
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryRecursiveTreeCommand<T> extends AbstractSelectQueryCommand<T> {

    /**
     * 递归的树主表初始条目查询的条件
     * 注意：条件中只有主表字段条件，不能使用关联表字段条件；当为空时查询所有数据不递归，因此此时 recursiveDown 无意义。
     */
    Condition initNodeCondition;

    public static QueryRecursiveTreeCommand<Map<String, Object>> build() {
        return new QueryRecursiveTreeCommand<>();
    }


}
