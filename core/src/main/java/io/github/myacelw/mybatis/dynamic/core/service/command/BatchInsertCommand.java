package io.github.myacelw.mybatis.dynamic.core.service.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

/**
 * 批量插入命令
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchInsertCommand implements Command {

    @NonNull
    List<?> data;

    /**
     * 禁用主键生成
     */
    private boolean disableGenerateId;
}
