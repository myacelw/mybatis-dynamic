package io.github.myacelw.mybatis.dynamic.core.service.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

/**
 * 批量更新命令
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchUpdateCommand implements Command {

    @NonNull
    List<?> data;

    /**
     * 是否只更新非空字段，为true时null字段值被跳过不更新
     */
    boolean onlyUpdateNonNull;

    /**
     * 自定义更新字段，用于更新非标准字段或需要特殊处理的字段
     */
    List<UpdateCommand.CustomSet> customSetList;

}
