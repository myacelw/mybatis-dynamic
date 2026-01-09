package io.github.myacelw.mybatis.dynamic.spring.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.annotation.ToMany;
import io.github.myacelw.mybatis.dynamic.core.annotation.ToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.util.List;

/**
 * 基础树形实体抽象类
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
@FieldNameConstants
public abstract class BaseTreeEntity<ID, T extends BaseTreeEntity<ID, T>> extends BaseEntity<ID> {

    ID parentId;

    /**
     * 父节点
     */
    @ToOne
    T parent;

    /**
     * 下级节点
     */
    @ToMany
    List<T> children;

    /**
     * 排序字符串
     */
    @BasicField(ddlCharacterMaximumLength = 25)
    String orderString;

}
