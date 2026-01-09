package io.github.myacelw.mybatis.dynamic.core.ext;

import lombok.NonNull;

import java.util.Map;

/**
 * 扩展Bean接口。
 * 实体对象可实现该接口，在ext中实现可动态变更的属性。
 *
 * @author liuwei
 */
public interface ExtBean {

    String NAME = "ext";

    @NonNull
    Map<String, Object> getExt();

}
