package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.CustomSelectField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 抽象的带有查询返回列的命令
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractSelectQueryCommand<T> extends AbstractQueryCommand<T> {

    /**
     * 查询返回的字段，为null时返回全部有权限的字段
     */
    List<String> selectFields;

    /**
     * 自定义查询字段，主要用于返回函数的情况，例如："MATCH ($COL[0]) AGAINST (#{EXPR} IN BOOLEAN MODE)"
     */
    List<CustomSelectField> customSelectFields;

    /**
     * 拷贝属性设置
     */
    public void copyProperties(AbstractSelectQueryCommand<T> source) {
        super.copyProperties(source);
        setSelectFields(source.getSelectFields());
        setCustomSelectFields(source.getCustomSelectFields());
    }


}
