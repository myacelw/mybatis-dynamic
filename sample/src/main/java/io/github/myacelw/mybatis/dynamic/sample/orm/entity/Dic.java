package io.github.myacelw.mybatis.dynamic.sample.orm.entity;


import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import io.github.myacelw.mybatis.dynamic.core.annotation.ToMany;
import io.github.myacelw.mybatis.dynamic.core.annotation.ToOne;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import io.github.myacelw.mybatis.dynamic.sample.orm.entity.enums.Status;
import io.github.myacelw.mybatis.dynamic.spring.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.util.List;

/**
 * 数据字典
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldNameConstants
@Data
@Model(logicDelete = true, comment = "数据字典")
public class Dic extends BaseEntity<Integer> {

    /**
     * 字典名
     */
    @BasicField(ddlComment = "字典名", ddlCharacterMaximumLength = 20, ddlNotNull = true, ddlIndex = true, ddlIndexType = IndexType.UNIQUE)
    String name;

    /**
     * 状态
     */
    @BasicField(ddlComment = "状态", ddlNotNull = true)
    Status status;

    /**
     * 所属字典目录ID
     */
    @BasicField(ddlComment = "所属字典目录ID", ddlNotNull = true, ddlIndex = true)
    Integer dicDirectoryId;

    /**
     * 所属字典目录
     */
    @ToOne
    DicDirectory dicDirectory;

    /**
     * 字典项目列表
     */
    @ToMany
    List<DicItem> dicItemList;


}
