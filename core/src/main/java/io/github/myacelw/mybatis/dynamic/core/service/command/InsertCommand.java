package io.github.myacelw.mybatis.dynamic.core.service.command;

import lombok.*;

/**
 * 插入命令
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class InsertCommand implements Command {

    /**
     * 要插入的数据，可以是Map 或 实体对象
     */
    @NonNull
    Object data;

    /**
     * 是否禁用自动ID生成，禁止生成ID后将使用插入数据中的ID，默认false
     */
    boolean disableGenerateId;

}
