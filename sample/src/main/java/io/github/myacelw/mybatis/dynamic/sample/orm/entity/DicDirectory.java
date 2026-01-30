package io.github.myacelw.mybatis.dynamic.sample.orm.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import io.github.myacelw.mybatis.dynamic.spring.entity.BaseTreeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * 字典目录
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldNameConstants
@Data
@Model(logicDelete = true, comment = "Dictionary Directory")
public class DicDirectory extends BaseTreeEntity<Integer, DicDirectory> {

    /**
     * 目录名
     */
    @BasicField(ddlComment = "Directory Name", ddlNotNull = true)
    private String name;

}
