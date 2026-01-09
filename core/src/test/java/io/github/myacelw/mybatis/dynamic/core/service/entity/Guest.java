package io.github.myacelw.mybatis.dynamic.core.service.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.BasicField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Guest extends Person {

    String code;

    @BasicField(ddlIndex = true)
    Status status;

}
