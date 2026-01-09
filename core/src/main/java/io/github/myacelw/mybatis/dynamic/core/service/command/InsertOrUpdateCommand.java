package io.github.myacelw.mybatis.dynamic.core.service.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 插入或更新命令
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertOrUpdateCommand implements Command {

    /**
     * 要更新的数据
     */
    @NonNull
    Object data;


}
