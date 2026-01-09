package io.github.myacelw.mybatis.dynamic.core.service.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.IdField;
import io.github.myacelw.mybatis.dynamic.core.annotation.ToOne;
import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import lombok.Data;

@Data
@Model
public class UserAddress {
    @IdField
    String userId;

    @IdField(order = 1)
    String addressId;

    @ToOne(targetModel = "User")
    User user;

    @ToOne(targetModel = "Address")
    Address address;


}
