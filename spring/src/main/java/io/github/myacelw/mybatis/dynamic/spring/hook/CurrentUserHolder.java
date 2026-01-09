package io.github.myacelw.mybatis.dynamic.spring.hook;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.Permission;

/**
 * 获取当前用户ID和当前用户权限的接口，
 * 实现该接口的类注册为Bean后可被DAO数据增删改查时使用。
 *
 * @author liuwei
 */
public interface CurrentUserHolder {

    /**
     * 得到当前用户ID，增删该时写入创建人修改人时使用。
     */
    String getCurrentUserId();

    /**
     * 得到当前用户权限，DAO增删改查数据时使用。
     */
    default Permission getCurrentUserPermission(Model model){
        return null;
    };

}
