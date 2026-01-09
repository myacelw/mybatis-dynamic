package io.github.myacelw.mybatis.dynamic.core.service.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 查询一条数据命令
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryOneCommand<T> extends AbstractSelectQueryCommand<T> {

    /**
     * 查询数据为空时抛出异常
     */
    boolean nullThrowException = false;

    /**
     * 只返回第一条数据
     */
    boolean onlyFirst = false;


    public static QueryOneCommand<Map<String, Object>> build() {
        return new QueryOneCommand<>();
    }

}
