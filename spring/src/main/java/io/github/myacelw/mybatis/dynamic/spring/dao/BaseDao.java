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
/**
 * 基本的DAO接口，提供针对特定实体的通用数据库操作方法。
 * <p>
 * 该接口是对 {@link DataManager} 的高层封装，集成了实体类型信息，简化了 Spring 环境下的数据访问层代码。
 *
 * @param <ID> 实体主键类型。
 * @param <T>  实体类类型。
 * @author liuwei
 */
public interface BaseDao<ID, T> {
    /**
     * 获取当前 DAO 关联的实体类。
     *
     * @return 实体类的 {@link Class} 对象。
     */
    Class<T> getEntityClass();

    /**
     * 获取底层的 {@link DataManager} 实例。
     *
     * @return DataManager 实例。
     */
    DataManager<ID> getDataManager();

    /**
     * 插入单条实体数据。
     *
     * @param data 要插入的实体对象。
     * @return 插入成功后的数据ID。
     */
    default ID insert(@NonNull T data) {
        return getDataManager().insert(data);
    }

    /**
     * 插入单条实体数据，并禁用 ID 自动生成。
     * <p>
     * 适用于手动指定主键值的场景。
     *
     * @param data 包含主键值的实体对象。
     * @return 插入的数据ID。
     */
    default ID insertDisableGenerateId(@NonNull T data) {
        return getDataManager().insertDisableGenerateId(data);
    }

    /**
     * 批量插入多条实体数据。
     *
     * @param data 实体对象列表。
     * @return 插入成功后的数据ID列表。
     */
    default List<ID> batchInsert(@NonNull List<T> data) {
        return getDataManager().batchInsert(data);
    }

    /**
     * 获取更新操作的处理链。
     *
     * @return UpdateChain 实例。
     */
    default UpdateChain<ID> update() {
        return getDataManager().updateChain();
    }

    /**
     * 全量更新实体数据。
     * <p>
     * 根据对象中的 ID 匹配数据库记录，更新所有字段（包括 null 值字段）。
     *
     * @param data 包含新值的实体对象。
     */
    default void update(@NonNull T data) {
        getDataManager().update(data);
    }

    /**
     * 更新实体数据中的非空字段。
     * <p>
     * 仅将实体对象中非 null 的字段更新到数据库。
     *
     * @param data 包含新值的实体对象。
     */
    default void updateOnlyNonNull(@NonNull T data) {
        getDataManager().updateOnlyNonNull(data);
    }

    /**
     * 批量更新多条实体数据。
     *
     * @param data 实体对象列表。
     */
    default void batchUpdate(@NonNull List<T> data) {
        getDataManager().batchUpdate(data);
    }

    /**
     * 批量更新多条实体数据中的非空字段。
     *
     * @param data 实体对象列表。
     */
    default void batchUpdateNonNull(@NonNull List<T> data) {
        getDataManager().batchUpdateNonNull(data);
    }

    /**
     * 根据主键状态（是否存在 ID）决定执行插入或更新操作。
     *
     * @param data 实体对象。
     * @return 数据 ID。
     */
    default ID insertOrUpdate(@NonNull T data) {
        return getDataManager().insertOrUpdate(data);
    }

    /**
     * 获取按条件批量更新操作的处理链。
     *
     * @return BatchUpdateByConditionChain 实例。
     */
    default BatchUpdateByConditionChain<ID> batchUpdateByCondition() {
        return getDataManager().batchUpdateByConditionChain();
    }

    /**
     * 批量插入或更新多条记录。
     *
     * @param data 数据对象列表（可以是实体或 Map）。
     */
    default void batchInsertOrUpdate(@NonNull List<T> data) {
        getDataManager().batchInsertOrUpdate(data);
    }

    /**
     * 根据 ID 删除记录。
     *
     * @param id 数据 ID。
     * @return 是否删除成功。
     */
    default boolean delete(@NonNull ID id) {
        return getDataManager().delete(id);
    }

    /**
     * 根据条件批量删除记录。
     *
     * @param condition 预定义的 Condition 对象。
     * @return 被删除的记录条数。
     */
    default int delete(@NonNull Condition condition) {
        return getDataManager().delete(condition);
    }

    /**
     * 根据条件批量删除记录。
     *
     * @param condition 条件构建器回调。
     * @return 被删除的记录条数。
     */
    default int delete(Consumer<ConditionBuilder> condition) {
        return getDataManager().delete(condition);
    }

    /**
     * 根据 ID 集合批量删除记录。
     *
     * @param idList ID 集合。
     * @return 被删除的记录条数。
     */
    default int batchDelete(@NonNull Collection<ID> idList) {
        return getDataManager().batchDelete(idList);
    }

    /**
     * 根据 ID 删除记录，并支持强制物理删除。
     *
     * @param id                  数据 ID。
     * @param forcePhysicalDelete 是否强制物理删除。
     * @return 是否删除成功。
     */
    default boolean delete(@NonNull ID id, boolean forcePhysicalDelete) {
        return getDataManager().delete(id, forcePhysicalDelete);
    }

    /**
     * 根据条件批量删除记录，并支持强制物理删除。
     *
     * @param condition           条件对象。
     * @param forcePhysicalDelete 是否强制物理删除。
     * @return 被删除的记录条数。
     */
    default int delete(Condition condition, boolean forcePhysicalDelete) {
        return getDataManager().delete(condition, forcePhysicalDelete);
    }

    /**
     * 根据条件批量删除记录，并支持强制物理删除。
     *
     * @param b                   条件构建器回调。
     * @param forcePhysicalDelete 是否强制物理删除。
     * @return 被删除的记录条数。
     */
    default int delete(Consumer<ConditionBuilder> b, boolean forcePhysicalDelete) {
        return getDataManager().delete(b, forcePhysicalDelete);
    }

    /**
     * 根据 ID 集合批量删除记录，并支持强制物理删除。
     *
     * @param idList              ID 集合。
     * @param forcePhysicalDelete 是否强制物理删除。
     * @return 被删除的记录条数。
     */
    default int batchDelete(@NonNull Collection<ID> idList, boolean forcePhysicalDelete) {
        return getDataManager().batchDelete(idList, forcePhysicalDelete);
    }

    /**
     * 根据 ID 查询实体记录。
     *
     * @param id 数据 ID。
     * @return 实体对象，不存在则返回 null。
     */
    default T getById(@NonNull ID id) {
        return getDataManager().getById(id, getEntityClass());
    }

    /**
     * 根据 ID 查询实体记录，并限定查询字段。
     *
     * @param id           数据 ID。
     * @param selectFields 要查询的字段路径列表。
     * @return 实体对象。
     */
    default T getById(@NonNull ID id, List<String> selectFields) {
        return getDataManager().getByIdChain(getEntityClass()).id(id).selectFields(selectFields).exec();
    }

    /**
     * 获取按 ID 查询操作的处理链。
     *
     * @return QueryByIdChain 实例。
     */
    default QueryByIdChain<ID, T> getById() {
        return getDataManager().getByIdChain(getEntityClass());
    }

    /**
     * 获取关联字段数据填充操作的处理链。
     *
     * @return FillDataChain 实例。
     */
    default FillDataChain<ID> fill() {
        return getDataManager().fillChain();
    }

    /**
     * 获取普通查询操作的处理链。
     *
     * @return QueryChain 实例。
     **/
    default QueryChain<ID, T> query() {
        return getDataManager().queryChain(getEntityClass());
    }

    /**
     * 根据条件查询实体列表。
     *
     * @param condition 预定义条件。
     * @return 实体列表。
     */
    default List<T> query(Condition condition) {
        return query().where(condition).exec();
    }

    /**
     * 根据条件查询实体列表。
     *
     * @param condition 条件构建器回调。
     * @return 实体列表。
     */
    default List<T> query(Consumer<ConditionBuilder> condition) {
        return query().where(condition).exec();
    }

    /**
     * 获取查询结果回调处理操作的处理链。
     *
     * @return QueryCallBackChain 实例。
     */
    default QueryCallBackChain<ID, T> queryCallBack() {
        return getDataManager().queryCallBackChain(getEntityClass());
    }

    /**
     * 执行带回调的大数据量查询。
     *
     * @param condition 查询条件。
     * @param handler   结果处理器。
     * @return 处理的记录条数。
     */
    default int queryCallBack(Condition condition, ResultHandler<T> handler) {
        return queryCallBack().where(condition).handler(handler).exec();
    }

    /**
     * 执行带回调的大数据量查询。
     *
     * @param condition 条件构建器回调。
     * @param handler   结果处理器。
     * @return 处理的记录条数。
     */
    default int queryCallBack(Consumer<ConditionBuilder> condition, ResultHandler<T> handler) {
        return queryCallBack().where(condition).handler(handler).exec();
    }

    /**
     * 获取流式查询操作的处理链。
     *
     * @return QueryCursorChain 实例。
     **/
    default QueryCursorChain<ID, T> queryCursor() {
        return getDataManager().queryCursorChain(getEntityClass());
    }

    /**
     * 开启流式查询。
     *
     * @param condition 查询条件。
     * @return Cursor 实例。
     */
    default Cursor<T> queryCursor(Condition condition) {
        return queryCursor().where(condition).exec();
    }

    /**
     * 开启流式查询。
     *
     * @param condition 条件构建器回调。
     * @return Cursor 实例。
     */
    default Cursor<T> queryCursor(Consumer<ConditionBuilder> condition) {
        return queryCursor().where(condition).exec();
    }

    /**
     * 获取分页查询操作的处理链。
     *
     * @return PageChain 实例。
     */
    default PageChain<ID, T> page() {
        return getDataManager().pageChain(getEntityClass());
    }

    /**
     * 执行分页查询。
     *
     * @param condition   条件构建器回调。
     * @param pageCurrent 当前页码。
     * @param pageSize    每页条数。
     * @param orderItems  排序规则。
     * @return PageResult 对象。
     */
    default PageResult<T> page(Consumer<ConditionBuilder> condition, int pageCurrent, int pageSize, OrderItem... orderItems) {
        return page().where(condition).orderItems(orderItems).page(pageCurrent, pageSize).exec();
    }

    /**
     * 获取单条记录查询操作的处理链。
     *
     * @return QueryOneChain 实例。
     */
    default QueryOneChain<ID, T> queryOne() {
        return getDataManager().queryOneChain(getEntityClass());
    }

    /**
     * 查询单条实体记录。
     *
     * @param condition 预定义条件。
     * @return 实体对象。
     */
    default T queryOne(Condition condition) {
        return queryOne().where(condition).exec();
    }

    /**
     * 查询单条实体记录。
     *
     * @param conditionBuilderConfig 条件构建器回调。
     * @return 实体对象。
     */
    default T queryOne(Consumer<ConditionBuilder> conditionBuilderConfig) {
        return queryOne().where(conditionBuilderConfig).exec();
    }

    /**
     * 查询当前模型的全部记录。
     *
     * @return 实体列表。
     */
    default List<T> list() {
        return query().exec();
    }

    /**
     * 统计当前模型的总记录条数。
     *
     * @return 总条数。
     */
    default int count() {
        return getDataManager().count();
    }

    /**
     * 根据条件统计记录条数。
     *
     * @param condition 查询条件。
     * @return 记录条数。
     */
    default int count(Condition condition) {
        return getDataManager().count(condition);
    }

    /**
     * 根据条件统计记录条数。
     *
     * @param condition 条件构建器回调。
     * @return 记录条数。
     */
    default int count(Consumer<ConditionBuilder> condition) {
        return getDataManager().count(condition);
    }

    /**
     * 获取判断记录是否存在操作的处理链。
     *
     * @return ExistsChain 实例。
     */
    default ExistsChain<ID> exists() {
        return getDataManager().existsChain();
    }

    /**
     * 判断符合条件的记录是否存在。
     *
     * @param condition 查询条件。
     * @return 存在则返回 true。
     */
    default boolean exists(Condition condition) {
        return getDataManager().existsChain().where(condition).exec();
    }

    /**
     * 判断符合条件的记录是否存在。
     *
     * @param condition 条件构建器回调。
     * @return 存在则返回 true。
     */
    default boolean exists(Consumer<ConditionBuilder> condition) {
        return getDataManager().exists(condition);
    }

    /**
     * 获取汇总查询操作的处理链。
     *
     * @param clazz 结果转换类。
     * @param <K>   结果类型。
     * @return AggQueryChain 实例。
     **/
    default <K> AggQueryChain<ID, K> aggQuery(Class<K> clazz) {
        return getDataManager().aggQuery(clazz);
    }

    /**
     * 获取汇总查询操作的处理链（Map格式）。
     *
     * @return AggQueryChain 实例。
     **/
    default AggQueryChain<ID, Map<String, Object>> aggQuery() {
        return getDataManager().aggQuery();
    }

}

