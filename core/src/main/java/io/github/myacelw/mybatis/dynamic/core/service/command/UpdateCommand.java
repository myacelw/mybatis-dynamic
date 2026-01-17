package io.github.myacelw.mybatis.dynamic.core.service.command;

import lombok.*;

import java.util.List;

/**
 * 更新命令
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class UpdateCommand<ID> implements Command {

    /**
     * 更新数据的ID
     */
    ID id;

    /**
     * 要更新的数据，可以是Map 或 实体对象
     */
    @NonNull
    Object data;


    /**
     * 是否强制更新，如果为false，会找到data中数据库中数据变化部分进行更新，如没有变化则不更新；如果为true，则不检测是否变换
     */
    boolean force;


    /**
     * 是否只更新非空字段，为true时null字段值被跳过不更新
     */
    boolean updateOnlyNonNull;

    /**
     * 要更新的字段，如果为空，则更新所有字段
     */
    List<String> updateFields;

    /**
     * 要忽略的字段，如果为空，则不忽略任何字段
     */
    List<String> ignoreFields;

    /**
     * 是否忽略逻辑删除
     */
    boolean ignoreLogicDelete;


    /**
     * 自定义函数更新数据
     */
    List<CustomSet> customSetList;

    @Data
    @Builder
    public static class CustomSet {
        /**
         * 更新字段名
         */
        String updateField;

        /**
         * 条件sql语句模板
         */
        String sqlTemplate;

        String[] fields;

        Object value;
    }
}
