package io.github.myacelw.mybatis.dynamic.core.service.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import io.github.myacelw.mybatis.dynamic.core.annotation.GroupField;
import io.github.myacelw.mybatis.dynamic.core.annotation.ToMany;
import io.github.myacelw.mybatis.dynamic.core.annotation.ToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class User extends Person {
    int age;

    @BasicField(ddlIndex = true)
    Status status;

    @BasicField(ddlIndex = true)
    String mainDepartmentId;

    @GroupField
    DateBetween validDateBetween;

    //映射为关系
    @ToOne(targetModel = "Department")
    Department mainDepartment;

    //映射为子表
    @ToMany(targetModel = "UserAddress", joinTargetFields = "userId")
    List<UserAddress> userAddressList;

    //映射为关系表
    @ToMany(targetModel = "UserDepartment", joinTargetFields = "userId")
    List<UserDepartment> userDepartmentList;


}
