package io.github.myacelw.mybatis.dynamic.core.service.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.session.ResultHandler;

import java.util.Map;

/**
 * 查询回调命令
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryCallBackCommand<T> extends AbstractSelectQueryCommand<T> {

    /**
     * 最大返回行数
     */
    private Integer limit;

    /**
     * 偏移量
     */
    private Integer offset;

    /**
     * 结果回调接口
     */
    ResultHandler<T> handler;


    public static QueryCallBackCommand<Map<String, Object>> build() {
        return new QueryCallBackCommand<>();
    }


}
