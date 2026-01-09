package io.github.myacelw.mybatis.dynamic.spring.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.annotation.IdField;
import io.github.myacelw.mybatis.dynamic.core.service.filler.CreateTimeFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.UpdateTimeFiller;
import io.github.myacelw.mybatis.dynamic.spring.filler.CreatorFiller;
import io.github.myacelw.mybatis.dynamic.spring.filler.ModifierFiller;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * 包含审计属性的实体抽象类
 *
 * @author liuwei
 */
@Data
@FieldNameConstants
public abstract class BaseEntity<ID> {

    @IdField(ddlCharacterMaximumLength = 32, ddlComment = "主键ID")
    ID id;

    /**
     * 创建人
     */
    @BasicField(ddlCharacterMaximumLength = 32, ddlComment = "创建人", fillerName = CreatorFiller.NAME)
    String creator;

    /**
     * 创建时间
     */
    @BasicField(ddlComment = "创建时间", fillerName = CreateTimeFiller.NAME)
    LocalDateTime createTime;

    /**
     * 修改人
     */
    @BasicField(ddlComment = "修改人", ddlCharacterMaximumLength = 32, fillerName = ModifierFiller.NAME)
    String modifier;

    /**
     * 修改时间
     */
    @BasicField(ddlComment = "修改时间", fillerName = UpdateTimeFiller.NAME)
    LocalDateTime updateTime;

}
