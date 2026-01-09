package io.github.myacelw.mybatis.dynamic.sample.orm.service;

import io.github.myacelw.mybatis.dynamic.sample.orm.entity.Dic;
import io.github.myacelw.mybatis.dynamic.sample.orm.entity.DicItem;
import io.github.myacelw.mybatis.dynamic.spring.dao.BaseDao;
import io.github.myacelw.mybatis.dynamic.spring.service.BaseServiceImpl;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.List;

/**
 * 字典服务
 *
 * @author liuwei
 */
@Service
public class DicServiceImpl extends BaseServiceImpl<Integer, Dic> {

    final BaseDao<Integer, DicItem> dicItemDao;

    public DicServiceImpl(BaseDao<Integer, Dic> dao, BaseDao<Integer, DicItem> dicItemDao) {
        super(dao);
        this.dicItemDao = dicItemDao;
    }

    /** 根据ID查询字典，同时关联查询字典项目列表 */
    @Override
    public Dic getById(@NonNull Integer id) {
        // 指定返回全部字段 和 查询字典项目列表，这会生成Join查询语句。
        return dao.getById(id, Arrays.asList("*", Dic.Fields.dicItemList));
    }

    @Transactional
    @Override
    public Integer insert(@NonNull Dic data) {
        Integer id = dao.insert(data);
        // 插入字典项目列表
        data.getDicItemList().forEach(t -> t.setDicId(id));
        dicItemDao.batchInsert(data.getDicItemList());
        return id;
    }

    /**
     * 更新字典，同时更新字典项目列表
     */
    @Transactional
    @Override
    public void update(@NonNull Dic data) {
        dao.update(data);
        dicItemDao.batchUpdate(data.getDicItemList());
    }

    @Transactional
    @Override
    public boolean delete(@PathVariable @NonNull Integer id) {
        boolean b = dao.delete(id);
        dicItemDao.delete(t->t.eq(DicItem::getDicId, id));
        return b;

    }

    public List<Dic> queryByExample() {
        List<DicItem> dicItemList = dicItemDao.query()
                .select(DicItem.Fields.key, DicItem.Fields.value, DicItem.Fields.dic + "." + Dic.Fields.name)
                .where(b -> b.eq(DicItem.Fields.dic + "." + Dic.Fields.name, "证件类型"))
                .asc(DicItem.Fields.key)
                .page(1, 10)
                .exec();


        List<Dic> dicList = dao.query(b -> b.exists(Dic::getDicItemList, b2 -> b2.eq(DicItem::getValue, "护照")));

        return dicList;
    }

}
