package io.github.myacelw.mybatis.dynamic.core.service.entity;

import io.github.myacelw.mybatis.dynamic.core.annotation.IdField;
import lombok.Data;

@Data
public class Base {
    @IdField(ddlCharacterMaximumLength = 32)
    String id;
}


