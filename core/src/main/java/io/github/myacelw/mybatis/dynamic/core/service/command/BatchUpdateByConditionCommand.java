package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

/**
 * 按条件批量更新命令
 *
 * @author conductor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchUpdateByConditionCommand implements Command {

    @NonNull
    private List<UpdatePair> updates;

    /**
     * 批量更新的大小，默认值为1000
     */
    int batchSize = 1000;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePair {
        @NonNull
        private Condition condition;
        @NonNull
        private Object data;

        /**
         * 是否只更新非空字段，为true时null字段值被跳过不更新
         */
        boolean updateOnlyNonNull;

        /**
         * 自定义更新字段
         */
        List<UpdateCommand.CustomSet> customSetList;
    }
}
