package io.github.myacelw.mybatis.dynamic.core.service.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Department extends Base {
    String name;
}
