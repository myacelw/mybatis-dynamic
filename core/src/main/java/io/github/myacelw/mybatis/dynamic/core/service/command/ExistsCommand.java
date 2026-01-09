package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询是否存在数据命令
 *
 * @author liuwei
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExistsCommand implements Command {

    /**
     * 查询条件。
     * 对于树形查询是 树形查询结束后，关联join后的过滤条件
     */
    Condition condition;

    /**
     * 关联查询涉及的字段及其对应模型、附加查询条件等；支持多级级联嵌套；
     * 可以对加入的ToOne 或 ToMany 类型字段 查询相关表数据。
     * 例如：查询User数据，可以级联查询 department 属性对应模型，和 department.company 对应模型。
     */
    List<Join> joins;

}
