package io.github.myacelw.mybatis.dynamic.sample.orm.entity.enums;

import lombok.Getter;

/**
 * 状态枚举
 *
 * @author liuwei
 */
@Getter
public enum Status {

    Valid("Valid"),
    Invalid("Invalid"),

    ;


    final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

}
