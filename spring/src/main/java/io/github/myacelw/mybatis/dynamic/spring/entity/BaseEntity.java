package io.github.myacelw.mybatis.dynamic.spring.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.annotation.IdField;
import io.github.myacelw.mybatis.dynamic.core.service.filler.CreateTimeFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.UpdateTimeFiller;
import io.github.myacelw.mybatis.dynamic.spring.filler.CreatorFiller;
import io.github.myacelw.mybatis.dynamic.spring.filler.ModifierFiller;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 包含审计属性的实体抽象类
 *
 * @author liuwei
 */
@Data
@FieldNameConstants
public abstract class BaseEntity<ID> implements Serializable {

    @IdField(ddlCharacterMaximumLength = 32, ddlComment = "Primary Key ID")
    private ID id;

    /**
     * 创建人
     */
    @BasicField(ddlCharacterMaximumLength = 32, ddlComment = "Creator", fillerName = CreatorFiller.NAME)
    private String creator;

    /**
     * 创建时间
     */
    @BasicField(ddlComment = "Create Time", fillerName = CreateTimeFiller.NAME)
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @BasicField(ddlComment = "Modifier", ddlCharacterMaximumLength = 32, fillerName = ModifierFiller.NAME)
    private String modifier;

    /**
     * 修改时间
     */
    @BasicField(ddlComment = "Update Time", fillerName = UpdateTimeFiller.NAME)
    private LocalDateTime updateTime;

}
