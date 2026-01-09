package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
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
     * 分页，这里的行数是join子表、关系表后的整体行数
     */
    Page page;

    /**
     * 结果回调接口
     */
    ResultHandler<T> handler;


    public static QueryCallBackCommand<Map<String, Object>> build() {
        return new QueryCallBackCommand<>();
    }


}
