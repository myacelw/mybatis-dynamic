package io.github.myacelw.mybatis.dynamic.core.service;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import lombok.NonNull;

/**
 * 实体类转换为模型接口
 *
 * @author liuwei
 */
public interface Class2ModelTransfer {
    /**
     * 将实体类转换为模型
     *
     * @param entityClass 实体类
     * @return 模型
     */
    Model getModelForClass(@NonNull Class<?> entityClass);
}
