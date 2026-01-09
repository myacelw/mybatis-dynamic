package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import lombok.Data;

import java.util.List;

/**
 * 抽象的查询命令
 *
 * @author liuwei
 */
@Data
public abstract class AbstractQueryCommand<T> implements Command {

    /**
     * 查询条件。
     * 对于树形查询是 树形查询结束后，关联join后的过滤条件
     */
    Condition condition;

    /**
     * 排序
     */
    List<OrderItem> orderItems;

    /**
     * 关联查询涉及的字段及其对应模型、附加查询条件等；支持多级级联嵌套；
     * 可以对加入的ToOne 或 ToMany 类型字段 查询相关表数据。
     * 例如：查询User数据，可以级联查询 department 属性对应模型，和 department.company 对应模型。
     */
    List<Join> joins;

    /**
     * 结果类型
     */
    Class<T> clazz;

    /**
     * 是否忽略逻辑删除
     */
    boolean ignoreLogicDelete;

    /**
     * 拷贝属性设置
     */
    public void copyProperties(AbstractQueryCommand<T> source){
        setCondition(source.getCondition());
        setJoins(source.getJoins());
        setOrderItems(source.getOrderItems());
        setClazz(source.getClazz());
        setIgnoreLogicDelete(source.isIgnoreLogicDelete());
    }

}
