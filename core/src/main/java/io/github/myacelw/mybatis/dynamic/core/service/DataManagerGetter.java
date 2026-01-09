package io.github.myacelw.mybatis.dynamic.core.service;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import org.apache.ibatis.session.SqlSession;

/**
 * 获取模型的数据管理器接口
 *
 * @author liuwei
 */
public interface DataManagerGetter {

    /**
     * 根据模型名称和字段获取数据管理器；
     * 当不考虑权限的情况下可以直接使用模型名得到模型，然后得到数据管理器。
     *
     * @param modelName  获取的模型名称
     * @param sqlSession mybatis会话
     * @return 获取模型的数据管理器
     */
    <ID> DataManager<ID> getDataManager(String modelName, SqlSession sqlSession);

    boolean isModelExist(String modelName);

    default ModelContext getModelContext(String modelName) {
        DataManager<?> dataManager = getDataManager(modelName, null);
        return dataManager.getModelContext();
    }

    default Model getModel(String modelName) {
        if (isModelExist(modelName)) {
            DataManager<?> dataManager = getDataManager(modelName, null);
            return dataManager.getModel();
        }
        return null;
    }
}