package io.github.myacelw.mybatis.dynamic.sample.orm.entity.enums;

import lombok.Getter;

/**
 * 状态枚举
 *
 * @author liuwei
 */
@Getter
public enum Status {
    Valid("有效"),
    Invalid("无效"),

    ;

    final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

}
