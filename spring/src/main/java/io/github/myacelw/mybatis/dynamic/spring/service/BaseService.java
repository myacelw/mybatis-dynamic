package io.github.myacelw.mybatis.dynamic.spring.service;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.PageResult;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.chain.*;
import io.github.myacelw.mybatis.dynamic.core.service.chain.*;
import lombok.NonNull;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.ResultHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 基本的Service接口
 *
 * @author liuwei
 */
public interface BaseService<ID, T> {

    Class<T> getEntityClass();

    DataManager<ID> getDataManager();

    /**
     * 插入数据
     */
    ID insert(@NonNull T data);

    /**
     * 指定Id插入数据
     */
    ID insertDisableGenerateId(@NonNull T data);

    /**
     * 批量插入数据
     */
    List<ID> batchInsert(@NonNull List<T> data);

    /**
     * 更新数据
     * 参数 data: 变化的数据项, 可以是实体对象、Map类型 或者 与实体存在同样字段的DTO类型；
     * 参数 id: 如果未指定id，则从data里得到id取值；
     * 参数 onlyUpdateNonNull: 如果为true，则只更新非空的字段，否则全部更新；
     * 参数 force : 强制更新；不比较数据是否发生了变化，主表减少了一次查询当前数据。
     */
    UpdateChain<ID> update();

    /**
     * 更新数据
     *
     * @param data 数据
     */
    void update(@NonNull T data);

    /**
     * 更新非空字段数据
     */
    void onlyUpdateNonNull(@NonNull T data);

    /**
     * 批量更新数据
     */
    void batchUpdate(@NonNull List<T> data);

    /**
     * 批量更新非空字段数据
     */
    void batchUpdateNonNull(@NonNull List<T> data);

    /**
     * 插入或者更新数据，根据是否存在id来决定是插入还是更新
     *
     * @param data 数据
     */
    ID insertOrUpdate(@NonNull T data);

    /**
     * 按ID删除数据，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     */
    boolean delete(@NonNull ID id);

    /**
     * 批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     */
    int delete(@NonNull Condition condition);

    /**
     * 批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     */
    int delete(Consumer<Condition.ConditionBuilder> condition);

    /**
     * 按ID列表批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     */
    int batchDelete(@NonNull Collection<ID> idList);

    /**
     * 按ID删除数据，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     */
    boolean delete(@NonNull ID id, boolean forcePhysicalDelete);

    /**
     * 按条件批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     */
    int delete(Condition condition, boolean forcePhysicalDelete);

    /**
     * 按条件批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     */
    int delete(Consumer<Condition.ConditionBuilder> b, boolean forcePhysicalDelete);

    /**
     * 按ID列表批量删除，根据是否存在逻辑删除字段，执行逻辑删除或者物理删除
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     */
    int batchDelete(@NonNull Collection<ID> idList, boolean forcePhysicalDelete);

    /**
     * 按照ID查询数据
     * @param id 数据ID
     * @param selectFields 要查询的字段，空时查询所有字段
     */
    T getById(@NonNull ID id, List<String> selectFields);

    /**
     * 按照ID查询数据
     */
    T getById(@NonNull ID id);

    /**
     * 按照ID查询数据
     */
    QueryByIdChain<ID, T> getById();

    /**
     * ToOne 或 ToMany 类型字段关联表的填充
     */
    FillDataChain<ID> fill();

    /**
     * 按条件查询数据，使用Left Join方式关联ToOne 或 ToMany 类型字段关联表查询。
     **/
    QueryChain<ID, T> query();

    /**
     * 按条件查询数据
     */
    List<T> query(Condition condition);

    /**
     * 按条件查询数据
     */
    List<T> query(Consumer<Condition.ConditionBuilder> condition);

    /**
     * 查询结果回调方式的查询，用于大数据量查询，如大数据量查询并生成Excel
     */
    QueryCallBackChain<ID, T> queryCallBack();

    /**
     * 查询结果回调方式的查询，用于大数据量查询，如大数据量查询并生成Excel
     * @param condition 查询条件
     * @param handler 结果处理器
     */
    int queryCallBack(Condition condition, ResultHandler<T> handler);

    /**
     * 查询结果回调方式的查询，用于大数据量查询，如大数据量查询并生成Excel
     * @param condition 查询条件构建器
     * @param handler 结果处理器
     */
    int queryCallBack(Consumer<Condition.ConditionBuilder> condition, ResultHandler<T> handler);

    /**
     * 流式数据查询，通过回调接口处理返回数据
     **/
    QueryCursorChain<ID, T> queryCursor();

    /**
     * 流式数据查询，通过回调接口处理返回数据
     * @param condition 查询条件
     */
    Cursor<T> queryCursor(Condition condition);

    /**
     * 流式数据查询，通过回调接口处理返回数据
     * @param condition 查询条件构建器
     */
    Cursor<T> queryCursor(Consumer<Condition.ConditionBuilder> condition);

    /**
     * 分页查询
     */
    PageChain<ID, T> page();

    /**
     * 分页查询
     * @param condition 查询条件构建器
     * @param pageCurrent 查询页号
     * @param pageSize 每页条数
     * @param orderItems 排序字段
     */
    PageResult<T> page(Consumer<Condition.ConditionBuilder> condition, int pageCurrent, int pageSize, OrderItem... orderItems);

    /**
     * 查询单条数据
     */
    QueryOneChain<ID, T> queryOne();

    /**
     * 查询单条数据
     */
    T queryOne(Condition condition);

    /**
     * 查询单条数据
     */
    T queryOne(Consumer<Condition.ConditionBuilder> conditionBuilderConfig);

    /**
     * 查询全部数据
     */
    List<T> list();

    /**
     * 查询全部数据条数
     */
    long count();

    /**
     * 按条件查询数据条数。
     *
     * @param condition 查询条件
     */
    long count(Condition condition);

    /**
     * 按条件查询数据条数。
     *
     * @param condition 查询条件构建器
     */
    long count(Consumer<Condition.ConditionBuilder> condition);

    /**
     * 按条件查询是否存在数据。
     */
    ExistsChain<ID> exists();

    /**
     * 按条件查询是否存在数据。
     *
     * @param condition 查询条件
     */
    boolean exists(Condition condition);

    /**
     * 按条件查询是否存在数据。
     *
     * @param condition 查询条件构建器
     */
    boolean exists(Consumer<Condition.ConditionBuilder> condition);

    /**
     * 汇总查询
     **/
    <K> AggQueryChain<ID, K> agg(Class<K> clazz);

    /**
     * 汇总查询
     **/
    AggQueryChain<ID, Map<String, Object>> agg();
}

