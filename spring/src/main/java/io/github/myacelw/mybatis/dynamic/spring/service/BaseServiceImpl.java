package io.github.myacelw.mybatis.dynamic.spring.service;

import io.github.myacelw.mybatis.dynamic.spring.dao.BaseDao;
import lombok.experimental.Delegate;

/**
 * 基本的Service实现类
 *
 * @author liuwei
 */
public class BaseServiceImpl<ID, T> implements BaseService<ID, T> {

    @Delegate
    protected BaseDao<ID, T> dao;

    public BaseServiceImpl(BaseDao<ID, T> dao) {
        this.dao = dao;
    }



}

