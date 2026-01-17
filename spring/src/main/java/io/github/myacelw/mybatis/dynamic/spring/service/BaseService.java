package io.github.myacelw.mybatis.dynamic.spring.service;

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
 * 基本的 Service 接口，定义了针对特定实体的核心业务逻辑。
 * <p>
 * 该接口通常由具体的业务 Service 继承，提供了 CRUD、分页、流式查询、统计等常用操作的定义。
 * 它是应用服务层与持久层（DAO）之间的核心抽象。
 *
 * @param <ID> 实体主键类型。
 * @param <T>  实体类类型。
 * @author liuwei
 */
public interface BaseService<ID, T> {

    /**
     * 获取当前 Service 关联的实体类。
     *
     * @return 实体类的 {@link Class} 对象。
     */
    Class<T> getEntityClass();

    /**
     * 获取当前 Service 使用的 {@link DataManager} 实例。
     *
     * @return DataManager 实例。
     */
    DataManager<ID> getDataManager();

    /**
     * 插入单条数据。
     *
     * @param data 要插入的实体对象。
     * @return 插入成功后的数据 ID。
     */
    ID insert(@NonNull T data);

    /**
     * 插入单条数据，并禁用 ID 自动生成。
     *
     * @param data 包含预设主键值的实体对象。
     * @return 数据 ID。
     */
    ID insertDisableGenerateId(@NonNull T data);

    /**
     * 批量插入多条数据。
     *
     * @param data 实体对象列表。
     * @return 插入成功后的数据 ID 列表。
     */
    List<ID> batchInsert(@NonNull List<T> data);

    /**
     * 获取更新操作的处理链。
     *
     * @return UpdateChain 实例。
     */
    UpdateChain<ID> update();

    /**
     * 全量更新实体数据。
     *
     * @param data 包含新值的实体对象。
     */
    void update(@NonNull T data);

    /**
     * 更新实体数据中的非空字段。
     *
     * @param data 包含新值的实体对象。
     */
    void updateOnlyNonNull(@NonNull T data);

    /**
     * 批量更新多条数据。
     *
     * @param data 实体对象列表。
     */
    void batchUpdate(@NonNull List<T> data);

    /**
     * 批量更新多条数据中的非空字段。
     *
     * @param data 实体对象列表。
     */
    void batchUpdateNonNull(@NonNull List<T> data);

    /**
     * 获取按条件批量更新操作的处理链。
     *
     * @return BatchUpdateByConditionChain 实例。
     */
    BatchUpdateByConditionChain<ID> batchUpdateByConditionChain();

    /**
     * 根据 ID 是否存在决定执行插入或更新。
     *
     * @param data 实体对象。
     * @return 数据 ID。
     */
    ID insertOrUpdate(@NonNull T data);

    /**
     * 根据 ID 删除记录。
     *
     * @param id 数据 ID。
     * @return 是否删除成功。
     */
    boolean delete(@NonNull ID id);

    /**
     * 根据条件批量删除记录。
     *
     * @param condition 预定义条件。
     * @return 被删除记录数。
     */
    int delete(@NonNull Condition condition);

    /**
     * 根据条件批量删除记录。
     *
     * @param condition 条件构建器回调。
     * @return 被删除记录数。
     */
    int delete(Consumer<ConditionBuilder> condition);

    /**
     * 根据 ID 集合批量删除记录。
     *
     * @param idList ID 集合。
     * @return 被删除记录数。
     */
    int batchDelete(@NonNull Collection<ID> idList);

    /**
     * 根据 ID 删除记录，并支持强制物理删除。
     *
     * @param id                  数据 ID。
     * @param forcePhysicalDelete 是否强制物理删除。
     * @return 是否成功。
     */
    boolean delete(@NonNull ID id, boolean forcePhysicalDelete);

    /**
     * 根据条件批量删除记录，并支持强制物理删除。
     *
     * @param condition           条件对象。
     * @param forcePhysicalDelete 是否强制物理删除。
     * @return 被删除记录数。
     */
    int delete(Condition condition, boolean forcePhysicalDelete);

    /**
     * 根据条件批量删除记录，并支持强制物理删除。
     *
     * @param b                   条件构建器。
     * @param forcePhysicalDelete 是否强制物理删除。
     * @return 被删除记录数。
     */
    int delete(Consumer<ConditionBuilder> b, boolean forcePhysicalDelete);

    /**
     * 根据 ID 集合批量删除记录，并支持强制物理删除。
     *
     * @param idList              ID 集合。
     * @param forcePhysicalDelete 是否强制物理删除。
     * @return 被删除记录数。
     */
    int batchDelete(@NonNull Collection<ID> idList, boolean forcePhysicalDelete);

    /**
     * 根据 ID 查询实体并限定字段。
     *
     * @param id           数据 ID。
     * @param selectFields 限定字段列表。
     * @return 实体对象。
     */
    T getById(@NonNull ID id, List<String> selectFields);

    /**
     * 根据 ID 查询实体。
     *
     * @param id 数据 ID。
     * @return 实体对象。
     */
    T getById(@NonNull ID id);

    /**
     * 获取按 ID 查询操作的处理链。
     *
     * @return QueryByIdChain 实例。
     */
    QueryByIdChain<ID, T> getById();

    /**
     * 获取关联字段数据填充操作的处理链。
     *
     * @return FillDataChain 实例。
     */
    FillDataChain<ID> fill();

    /**
     * 获取普通查询操作的处理链。
     *
     * @return QueryChain 实例。
     **/
    QueryChain<ID, T> query();

    /**
     * 根据条件查询实体列表。
     *
     * @param condition 预定义条件。
     * @return 实体列表。
     */
    List<T> query(Condition condition);

    /**
     * 根据条件查询实体列表。
     *
     * @param condition 条件构建器。
     * @return 实体列表。
     */
    List<T> query(Consumer<ConditionBuilder> condition);

    /**
     * 获取查询结果回调处理操作的处理链。
     *
     * @return QueryCallBackChain 实例。
     */
    QueryCallBackChain<ID, T> queryCallBack();

    /**
     * 执行带回调的查询。
     *
     * @param condition 查询条件。
     * @param handler   结果处理器。
     * @return 记录条数。
     */
    int queryCallBack(Condition condition, ResultHandler<T> handler);

    /**
     * 执行带回调的查询。
     *
     * @param condition 条件构建器。
     * @param handler   结果处理器。
     * @return 记录条数。
     */
    int queryCallBack(Consumer<ConditionBuilder> condition, ResultHandler<T> handler);

    /**
     * 获取流式查询操作的处理链。
     *
     * @return QueryCursorChain 实例。
     **/
    QueryCursorChain<ID, T> queryCursor();

    /**
     * 开启流式查询。
     *
     * @param condition 查询条件。
     * @return Cursor 对象。
     */
    Cursor<T> queryCursor(Condition condition);

    /**
     * 开启流式查询。
     *
     * @param condition 条件构建器。
     * @return Cursor 对象。
     */
    Cursor<T> queryCursor(Consumer<ConditionBuilder> condition);

    /**
     * 获取分页查询操作的处理链。
     *
     * @return PageChain 实例。
     */
    PageChain<ID, T> page();

    /**
     * 执行分页查询。
     *
     * @param condition   条件构建器。
     * @param pageCurrent 当前页码。
     * @param pageSize    页大小。
     * @param orderItems  排序规则。
     * @return PageResult 对象。
     */
    PageResult<T> page(Consumer<ConditionBuilder> condition, int pageCurrent, int pageSize, OrderItem... orderItems);

    /**
     * 获取单条记录查询操作的处理链。
     *
     * @return QueryOneChain 实例。
     */
    QueryOneChain<ID, T> queryOne();

    /**
     * 查询单条记录。
     *
     * @param condition 预定义条件。
     * @return 实体对象。
     */
    T queryOne(Condition condition);

    /**
     * 查询单条记录。
     *
     * @param conditionBuilderConfig 条件构建器。
     * @return 实体对象。
     */
    T queryOne(Consumer<ConditionBuilder> conditionBuilderConfig);

    /**
     * 查询全部记录。
     *
     * @return 实体列表。
     */
    List<T> list();

    /**
     * 统计总记录数。
     *
     * @return 记录总数。
     */
    int count();

    /**
     * 按条件统计记录数。
     *
     * @param condition 预定义条件。
     * @return 记录数。
     */
    int count(Condition condition);

    /**
     * 按条件统计记录数。
     *
     * @param condition 条件构建器。
     * @return 记录数。
     */
    int count(Consumer<ConditionBuilder> condition);

    /**
     * 获取判断是否存在操作的处理链。
     *
     * @return ExistsChain 实例。
     */
    ExistsChain<ID> exists();

    /**
     * 判断符合条件的记录是否存在。
     *
     * @param condition 预定义条件。
     * @return 存在则返回 true。
     */
    boolean exists(Condition condition);

    /**
     * 判断符合条件的记录是否存在。
     *
     * @param condition 条件构建器。
     * @return 存在则返回 true。
     */
    boolean exists(Consumer<ConditionBuilder> condition);

    /**
     * 获取汇总查询操作的处理链。
     *
     * @param clazz 结果转换类。
     * @param <K>   结果类型。
     * @return AggQueryChain 实例。
     **/
    <K> AggQueryChain<ID, K> aggQuery(Class<K> clazz);

    /**
     * 获取汇总查询操作的处理链（Map格式）。
     *
     * @return AggQueryChain 实例。
     **/
    AggQueryChain<ID, Map<String, Object>> aggQuery();
}

