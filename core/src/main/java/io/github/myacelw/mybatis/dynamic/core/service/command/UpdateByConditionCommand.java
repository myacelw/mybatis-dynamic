package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 按条件删除命令
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateByConditionCommand implements Command {

    /**
     * 查询条件。
     */
    Condition condition;

    /**
     * 要更新的数据，可以是Map 或 实体对象
     */
    Object data;

    /**
     * 是否只更新非空字段，为true时null字段值被跳过不更新
     */
    boolean onlyUpdateNonNull;


    /**
     * 自定义函数更新数据
     */
    List<UpdateCommand.CustomSet> customSetList;

}
