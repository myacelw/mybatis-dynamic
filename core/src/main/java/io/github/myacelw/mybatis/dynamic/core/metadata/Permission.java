package io.github.myacelw.mybatis.dynamic.core.metadata;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 授权。
 * 包括列权限，行权限。
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    /**
     * 字段权限（列权限），如果为空则表示不限制；
     * 列表中为字段名
     */
    List<String> fieldRights;

    /**
     * 数据权限（行权限），如果为空则表示不限制；
     */
    Condition dataRights;

}
