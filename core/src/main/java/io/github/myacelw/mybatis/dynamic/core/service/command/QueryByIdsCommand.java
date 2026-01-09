package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 按ID列表查询数据命令
 *
 * @author liuwei
 */
@Data
public class QueryByIdsCommand<ID, T> implements Command {

    Collection<ID> ids;

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
     * 查询返回的字段，为null时返回全部有权限的字段
     */
    List<String> selectFields;


    public static <ID> QueryByIdsCommand<ID, Map<String, Object>> build() {
        return new QueryByIdsCommand<>();
    }

}
