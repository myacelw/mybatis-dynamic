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
@Model(logicDelete = true, comment = "Dictionary Item")
public class DicItem extends BaseEntity<Integer> {

    @BasicField(ddlComment = "Belongs to Dictionary ID", ddlNotNull = true)
    private Integer dicId;

    /**
     * 字典项键
     */
    @BasicField(ddlComment = "Item Key", ddlCharacterMaximumLength = 20, ddlNotNull = true)
    private String key;

    /**
     * 字典项值
     */
    @BasicField(ddlComment = "Item Value", ddlCharacterMaximumLength = 20, ddlNotNull = true)
    private String value;

    /**
     * 字典项备注
     */
    @BasicField(ddlComment = "Item Comment", ddlCharacterMaximumLength = 200)
    private String comment;

    /**
     * 所属字典
     */
    @ToOne
    Dic dic;


}
