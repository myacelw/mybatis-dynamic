package io.github.myacelw.mybatis.dynamic.core.service;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.Permission;

/**
 * 获取权限接口
 *
 * @author liuwei
 */
public interface PermissionGetter {
    Permission getPermission(Model model);
}
