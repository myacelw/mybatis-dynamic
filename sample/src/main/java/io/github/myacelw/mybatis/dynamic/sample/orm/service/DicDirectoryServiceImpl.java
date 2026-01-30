package io.github.myacelw.mybatis.dynamic.sample.orm.service;

import io.github.myacelw.mybatis.dynamic.core.exception.data.DataException;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.sample.orm.entity.Dic;
import io.github.myacelw.mybatis.dynamic.sample.orm.entity.DicDirectory;
import io.github.myacelw.mybatis.dynamic.spring.dao.BaseDao;
import io.github.myacelw.mybatis.dynamic.spring.dao.BaseTreeDao;
import io.github.myacelw.mybatis.dynamic.spring.service.BaseTreeServiceImpl;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 字典目录服务
 *
 * @author liuwei
 */
@Service
public class DicDirectoryServiceImpl extends BaseTreeServiceImpl<Integer, DicDirectory> {

    private final BaseDao<Integer, Dic> dicDao;

    public DicDirectoryServiceImpl(BaseTreeDao<Integer, DicDirectory> dao, BaseDao<Integer, Dic> dicDao) {
        super(dao);
        this.dicDao = dicDao;
    }

    @Override
    public boolean delete(@PathVariable @NonNull Integer id) {
        long n = dao.count(Condition.builder().eq(DicDirectory::getParent, id).build());
        if (n > 0) {
            throw new DataException("Cannot delete DicDirectory with ID [" + id + "] as it contains sub-directories");
        }

        long n2 = dicDao.count(Condition.builder().eq(Dic::getDicDirectory, id).build());
        if (n2 > 0) {
            throw new DataException("Cannot delete DicDirectory with ID [" + id + "] as it contains data dictionaries");
        }
        return super.delete(id);
    }

}
