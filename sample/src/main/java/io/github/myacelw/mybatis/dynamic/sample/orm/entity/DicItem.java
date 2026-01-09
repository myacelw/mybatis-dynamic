package io.github.myacelw.mybatis.dynamic.sample.orm.entity;


import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import io.github.myacelw.mybatis.dynamic.core.annotation.ToOne;
import io.github.myacelw.mybatis.dynamic.spring.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * 字典项目
 *
 * @author liuwei
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldNameConstants
@Data
@Model(logicDelete = true, comment = "字典项目")
public class DicItem extends BaseEntity<Integer> {

    @BasicField(ddlComment = "所属字典ID", ddlNotNull = true)
    Integer dicId;

    /**
     * 字典项键
     */
    @BasicField(ddlComment = "字典项键", ddlCharacterMaximumLength = 20, ddlNotNull = true)
    String key;

    /**
     * 字典项值
     */
    @BasicField(ddlComment = "字典项值", ddlCharacterMaximumLength = 20, ddlNotNull = true)
    String value;

    /**
     * 字典项备注
     */
    @BasicField(ddlComment = "字典项备注", ddlCharacterMaximumLength = 200)
    String description;

    /**
     * 所属字典
     */
    @ToOne
    Dic dic;


}
