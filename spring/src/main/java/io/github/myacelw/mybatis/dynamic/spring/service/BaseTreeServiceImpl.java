package io.github.myacelw.mybatis.dynamic.spring.service;

import io.github.myacelw.mybatis.dynamic.spring.dao.BaseTreeDao;
import lombok.experimental.Delegate;

/**
 * 树形实体的基本Service实现类
 *
 * @author liuwei
 */
public class BaseTreeServiceImpl<ID, T> extends BaseServiceImpl<ID, T> implements BaseTreeService<ID, T> {

    @Delegate
    protected BaseTreeDao<ID, T> dao;

    public BaseTreeServiceImpl(BaseTreeDao<ID, T> dao) {
        super(dao);
        this.dao = dao;
    }

}

