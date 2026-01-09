package io.github.myacelw.mybatis.dynamic.core.service.command;

import lombok.*;

import java.util.Collection;

/**
 * 删除命令
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class DeleteCommand<ID> implements Command {

    /**
     * 删除的数据的ID列表
     */
    @NonNull
    Collection<ID> ids;

    /**
     * 是否强制物理删除
     */
    boolean forcePhysicalDelete;

    /**
     * 是否执行批量删除
     */
    boolean batch;

}
