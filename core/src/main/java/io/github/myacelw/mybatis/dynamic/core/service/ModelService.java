package io.github.myacelw.mybatis.dynamic.core.service;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.Permission;
import lombok.NonNull;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * 模型服务接口
 *
 * @author liuwei
 */
public interface ModelService extends DataManagerGetter {

    /**
     * 更新模型，变更存储数据涉及的数据库表结构、索引等。
     *
     * @param model          模型
     * @param fieldWhiteList 在此名单中的字段才涉及变更，空时不限制。
     */
    void update(@NonNull Model model, List<String> fieldWhiteList);

    /**
     * 更新模型，变更存储数据涉及的数据库表结构、索引等。
     *
     * @param model 模型
     */
    default void update(@NonNull Model model) {
        update(model, null);
    }

    /**
     * 从实体类获取对应模型，按照模型变更存储数据涉及的数据库表结构、索引等。
     *
     * @param entityClass 实体类
     */
    default Model update(@NonNull Class<?> entityClass) {
        Model model = getModelForClass(entityClass);
        update(model, null);
        return model;
    }

    /**
     * 删除模型存储数据涉及的数据库表，主要用于测试清空环境使用。
     */
    void delete(@NonNull Model model);

    /**
     * 删除模型存储数据涉及的数据库表，主要用于测试清空环境使用。
     */
    void delete(@NonNull Class<?> entityClass);

    /**
     * 将实体类转换为模型。
     *
     * @param entityClass 实体类， 可以使用 @Model 和 @Field 注解，增强模型说明。
     * @return 转换后得到的模型
     */
    Model getModelForClass(Class<?> entityClass);

    /**
     * 注册模型
     */
    void register(@NonNull Model model);

    /**
     * 注册实体模型
     */
    void register(@NonNull Class<?> entityClass, Model model);

    /**
     * 注册实体模型
     */
    default void register(@NonNull Class<?> entityClass) {
        register(entityClass, null);
    }

    /**
     * 反注册模型
     */
    Model unregister(@NonNull String modelName);

    /**
     * 获取所有已注册的模型
     *
     * @return 所有已注册的模型
     */
    List<Model> getAllRegisteredModels();

    /**
     * 更新并注册模型
     */
    Model updateAndRegister(@NonNull Model model);

    /**
     * 更新并注册模型
     */
    Model updateAndRegister(@NonNull Class<?> entityClass, Model model);

    /**
     * 更新并注册模型
     */
    default Model updateAndRegister(@NonNull Class<?> entityClass) {
        return updateAndRegister(entityClass, null);
    }

    /**
     * 创建某个模型的数据管理服务
     *
     * @param model      模型
     * @param permission 授权
     * @param sqlSession 会话，在该会话上执行SQL，如果为空则无事务
     */
    <ID> DataManager<ID> createDataManager(@NonNull Model model, Permission permission, SqlSession sqlSession);

    /**
     * 创建实体对应模型的数据服务
     *
     * @param entityClass 实体模型
     * @param permission  授权
     * @param sqlSession  会话，在该会话上执行SQL，如果为空则无事务
     */
    default <ID> DataManager<ID> createDataManager(@NonNull Class<?> entityClass, Permission permission, SqlSession sqlSession) {
        return createDataManager(getModelForClass(entityClass), permission, sqlSession);
    }

    /**
     * 从注册的模型中得到指定实体的模型管理器
     *
     * @param entityClass 实体模型
     * @param sqlSession  会话，在该会话上执行SQL，如果为空则无事务
     */
    <ID> DataManager<ID> getDataManager(@NonNull Class<?> entityClass, SqlSession sqlSession);

    default DataManager<Object[]> getDataManagerMultiId(@NonNull String modelName, SqlSession sqlSession) {
        return (DataManager) getDataManager(modelName, sqlSession);
    }

    default DataManager<Object[]> getDataManagerMultiId(@NonNull Class<?> entityClass, SqlSession sqlSession) {
        return (DataManager) getDataManager(entityClass, sqlSession);
    }

    ModelToTableConverter getModelToTableConverter();

}
