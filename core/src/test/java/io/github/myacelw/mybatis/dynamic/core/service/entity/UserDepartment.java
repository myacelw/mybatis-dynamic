package io.github.myacelw.mybatis.dynamic.core.service.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.IdField;
import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import io.github.myacelw.mybatis.dynamic.core.annotation.ToOne;
import lombok.Data;

@Data
@Model
public class UserDepartment {

    @IdField(ddlCharacterMaximumLength = 32)
    private String userId;

    @IdField(ddlCharacterMaximumLength = 32)
    private String departmentId;

    @ToOne
    private User user;

    @ToOne
    private Department department;
}
