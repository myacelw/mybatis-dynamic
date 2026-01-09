package io.github.myacelw.mybatis.dynamic.spring.dao;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.PageResult;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.ConditionBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.chain.*;
import lombok.NonNull;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.ResultHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 基本的DAO接口
 *
 * @author liuwei
 */
public interface BaseDao<ID, T> {
    /**
     * 获取DAO对应的实体类
     */
    Class<T> getEntityClass();

    /**
     * 获取数据管理器
     */
    DataManager<ID> getDataManager();

    /**
     * 插入数据
     */
    default ID insert(@NonNull T data) {
        return getDataManager().insert(data);
    }

    /**
     * 指定Id插入数据
     */
    default ID insertDisableGenerateId(@NonNull T data) {
        return getDataManager().insert(data, false);
    }

    /**
     * 批量插入数据
     */
    default List<ID> batchInsert(@NonNull List<T> data) {
        return getDataManager().batchInsert(data);
    }

    /**
     * 更新数据
     * 参数 data: 变化的数据项, 可以是实体对象、Map类型 或者 与实体存在同样字段的DTO类型；
     * 参数 id: 如果未指定id，则从data里得到id取值；
     * 参数 onlyUpdateNonNull: 如果为true，则只更新非空的字段，否则全部更新；
     * 参数 force : 强制更新；不比较数据是否发生了变化，主表减少了一次查询当前数据。
     */
    default UpdateChain<ID> update() {
        return getDataManager().updateChain();
    }

    /**
     * 更新数据
     *
     * @param data 数据
     */
    default void update(@NonNull T data) {
        update().data(data).force().exec();
    }

    /**
     * 更新非空字段数据
     */
    default void onlyUpdateNonNull(@NonNull T data) {
        update().data(data).onlyUpdateNonNull().exec();
    }

    /**
     * 批量更新数据
     */
    default void batchUpdate(@NonNull List<T> data) {
        data.forEach(this::update);
    }

    /**
     * 批量更新非空字段数据
     */
    default void batchUpdateNonNull(@NonNull List<T> data) {
        data.forEach(this::onlyUpdateNonNull);
    }

    /**
     * 插入或者更新数据，根据是否存在id来决定是插入还是更新
     *
     * @param data 数据
     */
    default ID insertOrUpdate(@NonNull T data) {
        return getDataManager().insertOrUpdate(data);
    }

    /**
     * 按ID删除数据，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     */
    default boolean delete(@NonNull ID id) {
        return getDataManager().delete(id);
    }

    /**
     * 批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     */
    default int delete(@NonNull Condition condition) {
        return getDataManager().delete(condition);
    }

    /**
     * 批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     */
    default int delete(Consumer<ConditionBuilder> condition) {
        return getDataManager().delete(condition);
    }

    /**
     * 按ID列表批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     */
    default int batchDelete(@NonNull Collection<ID> idList) {
        return getDataManager().batchDelete(idList);
    }

    /**
     * 按ID删除数据，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     */
    default boolean delete(@NonNull ID id, boolean forcePhysicalDelete) {
        return getDataManager().delete(id, forcePhysicalDelete);
    }

    /**
     * 按条件批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     */
    default int delete(Condition condition, boolean forcePhysicalDelete) {
        return getDataManager().delete(condition, forcePhysicalDelete);
    }

    /**
     * 按条件批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     */
    default int delete(Consumer<ConditionBuilder> b, boolean forcePhysicalDelete) {
        return getDataManager().delete(b, forcePhysicalDelete);
    }

    /**
     * 按ID列表批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     */
    default int batchDelete(@NonNull Collection<ID> idList, boolean forcePhysicalDelete) {
        return getDataManager().batchDelete(idList, forcePhysicalDelete);
    }

    /**
     * 按照ID查询数据
     * @param id 数据ID
     * @param selectFields 要查询的字段，空时查询所有字段
     */
    default T getById(@NonNull ID id, List<String> selectFields) {
        return getDataManager().getByIdChain(getEntityClass()).id(id).selectFields(selectFields).exec();
    }

    /**
     * 按照ID查询数据
     */
    default T getById(@NonNull ID id) {
        return getById(id, null);
    }

    /**
     * 按照ID查询数据
     */
    default QueryByIdChain<ID, T> getById() {
        return getDataManager().getByIdChain(getEntityClass());
    }

    /**
     * ToOne 或 ToMany 类型字段的填充
     */
    default FillDataChain<ID> fill() {
        return getDataManager().fillChain();
    }

    /**
     * 按条件查询数据，使用Left Join方式关联ToOne 或 ToMany 类型字段对应的表查询，查询条件支持关系表和子表条件。
     **/
    default QueryChain<ID, T> query() {
        return getDataManager().queryChain(getEntityClass());
    }

    /**
     * 按条件查询数据
     */
    default List<T> query(Condition condition) {
        return query().where(condition).exec();
    }

    /**
     * 按条件查询数据
     */
    default List<T> query(Consumer<ConditionBuilder> condition) {
        return query().where(condition).exec();
    }

    /**
     * 查询结果回调方式的查询，用于大数据量查询，如大数据量查询并生成Excel
     */
    default QueryCallBackChain<ID, T> queryCallBack() {
        return getDataManager().queryCallBackChain(getEntityClass());
    }

    /**
     * 查询结果回调方式的查询，用于大数据量查询，如大数据量查询并生成Excel
     * @param condition 查询条件
     * @param handler 结果处理器
     */
    default int queryCallBack(Condition condition, ResultHandler<T> handler) {
        return queryCallBack().where(condition).handler(handler).exec();
    }

    /**
     * 查询结果回调方式的查询，用于大数据量查询，如大数据量查询并生成Excel
     * @param condition 查询条件构建器
     * @param handler 结果处理器
     */
    default int queryCallBack(Consumer<ConditionBuilder> condition, ResultHandler<T> handler) {
        return queryCallBack().where(condition).handler(handler).exec();
    }

    /**
     * 流式数据查询，通过回调接口处理返回数据
     **/
    default QueryCursorChain<ID, T> queryCursor() {
        return getDataManager().queryCursorChain(getEntityClass());
    }

    /**
     * 流式数据查询，通过回调接口处理返回数据
     * @param condition 查询条件
     */
    default Cursor<T> queryCursor(Condition condition) {
        return queryCursor().where(condition).exec();
    }

    /**
     * 流式数据查询，通过回调接口处理返回数据
     * @param condition 查询条件构建器
     */
    default Cursor<T> queryCursor(Consumer<ConditionBuilder> condition) {
        return queryCursor().where(condition).exec();
    }

    /**
     * 分页查询
     */
    default PageChain<ID, T> page() {
        return getDataManager().pageChain(getEntityClass());
    }

    /**
     * 分页查询
     * @param condition 查询条件构建器
     * @param pageCurrent 查询页号
     * @param pageSize 每页条数
     * @param orderItems 排序字段
     */
    default PageResult<T> page(Consumer<ConditionBuilder> condition, int pageCurrent, int pageSize, OrderItem... orderItems) {
        return page().where(condition).orderItems(orderItems).page(pageCurrent, pageSize).exec();
    }

    /**
     * 查询单条数据
     */
    default QueryOneChain<ID, T> queryOne() {
        return getDataManager().queryOneChain(getEntityClass());
    }

    /**
     * 查询单条数据
     */
    default T queryOne(Condition condition) {
        return queryOne().where(condition).exec();
    }

    /**
     * 查询单条数据
     */
    default T queryOne(Consumer<ConditionBuilder> conditionBuilderConfig) {
        return queryOne().where(conditionBuilderConfig).exec();
    }

    /**
     * 查询全部数据
     */
    default List<T> list() {
        return query().exec();
    }

    /**
     * 查询全部数据条数
     */
    default long count() {
        return getDataManager().count();
    }

    /**
     * 按条件查询数据条数。
     *
     * @param condition 查询条件
     */
    default long count(Condition condition) {
        return getDataManager().count(condition);
    }

    /**
     * 按条件查询数据条数。
     *
     * @param condition 查询条件构建器
     */
    default long count(Consumer<ConditionBuilder> condition) {
        return getDataManager().count(condition);
    }

    /**
     * 按条件查询是否存在数据。
     */
    default ExistsChain<ID> exists() {
        return getDataManager().existsChain();
    }

    /**
     * 按条件查询是否存在数据。
     *
     * @param condition 查询条件
     */
    default boolean exists(Condition condition) {
        return getDataManager().existsChain().where(condition).exec();
    }

    /**
     * 按条件查询是否存在数据。
     *
     * @param condition 查询条件构建器
     */
    default boolean exists(Consumer<ConditionBuilder> condition) {
        return getDataManager().exists(condition);
    }

    /**
     * 汇总查询
     **/
    default <K> AggQueryChain<ID, K> agg(Class<K> clazz) {
        return getDataManager().aggQueryChain(clazz);
    }

    /**
     * 汇总查询
     **/
    default AggQueryChain<ID, Map<String, Object>> agg() {
        return getDataManager().aggQueryChain();
    }

}

