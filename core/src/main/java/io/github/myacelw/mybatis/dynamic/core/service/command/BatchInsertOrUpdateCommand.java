package io.github.myacelw.mybatis.dynamic.core.service.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

/**
 * 批量插入或更新命令
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchInsertOrUpdateCommand implements Command {

    @NonNull
    List<?> data;

    //更新操作忽略的字段
    List<String> updateIgnoreFields;

     /**
      * 批量更新的大小，默认值为1000
      */
    int batchSize = 1000;

}
