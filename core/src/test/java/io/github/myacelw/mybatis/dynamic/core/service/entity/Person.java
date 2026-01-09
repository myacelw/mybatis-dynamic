package io.github.myacelw.mybatis.dynamic.core.service.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.annotation.IgnoreField;
import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import io.github.myacelw.mybatis.dynamic.core.annotation.SubTypes;
import io.github.myacelw.mybatis.dynamic.core.annotation.partition.Partition;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Model(tableName = "person_custom_table",
        partition = @Partition(
                field = "id",
                key = 3
//                ,
//                level2 = @L2(
//                        field = "name",
//                        hash = 6
//                )
        )
)
@SubTypes(subTypes = {@SubTypes.SubType(User.class), @SubTypes.SubType(Guest.class)})
public abstract class Person extends Base {

    @BasicField(ddlCharacterMaximumLength = 200, ddlNotNull = true)
    String name;

    //修改模型，映射为FieldGroup，实现动态属性扩展
    @IgnoreField
    Map<String, Object> ext;

}
