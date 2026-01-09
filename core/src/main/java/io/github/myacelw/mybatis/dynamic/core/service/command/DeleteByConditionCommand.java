package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import lombok.*;

/**
 * 按条件删除命令
 *
 * @author liuwei
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class DeleteByConditionCommand implements Command {

    /**
     * 查询条件。
     */
    @NonNull
    final Condition condition;

    /**
     * 是否强制物理删除
     */
    boolean forcePhysicalDelete;

}
