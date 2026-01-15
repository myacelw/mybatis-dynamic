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
public class BatchInsertCommand implements Command {

    @NonNull
    List<?> data;

    /**
     * 禁用主键生成
     */
    private boolean disableGenerateId;

     /**
      * 批量插入的大小，默认值为1000
      */
    int batchSize = 1000;
}
