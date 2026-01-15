package io.github.myacelw.mybatis.dynamic.core.service;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.PageResult;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.ConditionBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.chain.*;
import io.github.myacelw.mybatis.dynamic.core.service.command.*;
import io.github.myacelw.mybatis.dynamic.core.service.impl.IdUtil;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import lombok.NonNull;
import org.apache.ibatis.session.ResultHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 数据管理器。
 * 对指定模型进行数据管理。
 *
 * @author liuwei
 */
public interface DataManager<ID> {

    /**
     * 数据管理器，对应的模型
     */
    Model getModel();

    /**
     * 模型上下文
     */
    ModelContext getModelContext();

    /**
     * 执行一个命令
     */
    <R> R execCommand(@NonNull Command command);

    /**
     * 插入数据
     *
     * @param data 数据，可以是Map 或 实体对象
     * @return 数据ID
     */
    default ID insert(@NonNull Object data) {
        return execCommand(new InsertCommand(data, false));
    }

    /**
     * 使用自定义的ID插入数据，禁用自动生成ID；也就是直接使用data中设置的主键值插入数据
     *
     * @param data              数据，可以是Map 或 实体对象
     * @return 数据ID
     */
    default ID insertDisableGenerateId(@NonNull Object data) {
        return execCommand(new InsertCommand(data, true));
    }

    /**
     * 批量插入数据
     *
     * @param data 数据列表，每个元素可以是Map 或 实体对象
     * @return 数据ID列表
     */
    default List<ID> batchInsert(@NonNull List<?> data) {
        BatchInsertCommand command = new BatchInsertCommand();
        command.setData(data);
        return execCommand(command);
    }

    /**
     * 更新数据
     */
    default UpdateChain<ID> updateChain() {
        return new UpdateChain<>(this);
    }

    /**
     * 更新数据
     *
     * @param id   数据ID
     * @param data 数据，可以是Map 或 实体对象
     */
    default void update(@NonNull ID id, @NonNull Object data) {
        updateChain().id(id).data(data).exec();
    }

    /**
     * 更新数据
     *
     * @param data 数据，可以是Map 或 实体对象
     */
    default void update(@NonNull Object data) {
        updateChain().data(data).exec();
    }

    /**
     * 更新非空字段数据
     */
    default void updateOnlyNonNull(@NonNull Object data) {
        updateChain().data(data).onlyUpdateNonNull().exec();
    }

    /**
     * 更新数据
     */
    default UpdateByConditionChain<ID> updateByConditionChain() {
        return new UpdateByConditionChain<>(this);
    }

    /**
     * 更新数据
     *
     * @param condition         更新条件构建器
     * @param data              数据，可以是Map 或 实体对象
     * @param onlyUpdateNonNull 是否只更新非空字段
     * @return 更新的记录数
     */
    default int updateByCondition(@NonNull Consumer<ConditionBuilder> condition, Object data, boolean onlyUpdateNonNull) {
        return updateByConditionChain().where(condition).data(data).onlyUpdateNonNull(onlyUpdateNonNull).exec();
    }

    /**
     * 批量更新数据
     *
     * @param data 数据列表，每个元素可以是Map 或 实体对象
     */
    default void batchUpdate(@NonNull List<?> data) {
        BatchUpdateCommand command = new BatchUpdateCommand();
        command.setData(data);
        execCommand(command);
    }

    /**
     * 批量更新数据非空字段
     *
     * @param data 数据列表，每个元素可以是Map 或 实体对象
     */
    default void batchUpdateNonNull(@NonNull List<?> data) {
        BatchUpdateCommand command = new BatchUpdateCommand();
        command.setData(data);
        command.setOnlyUpdateNonNull(true);
        execCommand(command);
    }

    /**
     * 插入或更新数据
     *
     * @param data 数据，可以是Map 或 实体对象
     */
    default ID insertOrUpdate(@NonNull Object data) {
        return execCommand(new InsertOrUpdateCommand(data));
    }


    /**
     * 批量插入或更新数据
     *
     * @param data 数据列表，每个元素可以是Map 或 实体对象
     */
    default void batchInsertOrUpdate(@NonNull List<?> data) {
        BatchInsertOrUpdateCommand command = new BatchInsertOrUpdateCommand();
        command.setData(data);
        execCommand(command);
    }

    /**
     * 删除数据。
     * 根据是否存在逻辑删除字段，执行逻辑删除或者物理删除。
     *
     * @param id 数据ID
     * @return 是否删除成功
     */
    default boolean delete(@NonNull ID id) {
        int n = execCommand(new DeleteCommand<>(Collections.singletonList(id)));
        return n > 0;
    }

    /**
     * 删除数据。
     * 根据是否存在逻辑删除字段，执行逻辑删除或者物理删除。
     *
     * @param id                  数据ID
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     * @return 是否删除成功
     */
    default boolean delete(@NonNull ID id, boolean forcePhysicalDelete) {
        DeleteCommand<ID> command = new DeleteCommand<>(Collections.singletonList(id));
        command.setForcePhysicalDelete(forcePhysicalDelete);
        int n = execCommand(command);
        return n > 0;
    }

    /**
     * 删除数据。
     * 根据是否存在逻辑删除字段，执行逻辑删除或者物理删除。
     *
     * @param condition 删除条件构建器
     * @return 删除的记录数
     */
    default int delete(Consumer<ConditionBuilder> condition) {
        return delete(condition, false);
    }

    /**
     * 删除数据。
     * 根据是否存在逻辑删除字段，执行逻辑删除或者物理删除。
     *
     * @param condition 删除条件构建器
     * @return 删除的记录数
     */
    default int delete(Condition condition) {
        return delete(condition, false);
    }


    /**
     * 删除数据。
     * 根据是否存在逻辑删除字段，执行逻辑删除或者物理删除。
     *
     * @param condition           删除条件构建器
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     */
    default int delete(Consumer<ConditionBuilder> condition, boolean forcePhysicalDelete) {
        ConditionBuilder conditionBuilder = new ConditionBuilder();
        condition.accept(conditionBuilder);
        return execCommand(new DeleteByConditionCommand(conditionBuilder.build(), forcePhysicalDelete));
    }

    /**
     * 删除数据。
     * 根据是否存在逻辑删除字段，执行逻辑删除或者物理删除。
     *
     * @param condition           删除条件构建器
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     * @return 删除的记录数
     */
    default int delete(Condition condition, boolean forcePhysicalDelete) {
        return execCommand(new DeleteByConditionCommand(condition, forcePhysicalDelete));
    }

    /**
     * 批量删除数据。
     * 根据是否存在逻辑删除字段，执行逻辑删除或者物理删除。
     *
     * @param idList 数据ID列表
     * @return 删除的记录数
     */
    default int batchDelete(@NonNull Collection<ID> idList) {
        return batchDelete(idList, false);
    }

    /**
     * 批量删除数据。
     * 根据是否存在逻辑删除字段，执行逻辑删除或者物理删除。
     *
     * @param idList              数据ID列表
     * @param forcePhysicalDelete 是否强制物理删除，不考虑是否存在逻辑删除字段
     * @return 删除的记录数
     */
    default int batchDelete(@NonNull Collection<ID> idList, boolean forcePhysicalDelete) {
        DeleteCommand<ID> command = new DeleteCommand<>(idList);
        command.setBatch(true);
        command.setForcePhysicalDelete(forcePhysicalDelete);
        return execCommand(command);
    }

    /**
     * 查询并填充关联字段、关联表字段、子表字段、扩展表字段的值
     **/
    default FillDataChain<ID> fillChain() {
        return new FillDataChain<>(this);
    }

    /**
     * 按照ID查询数据
     *
     * @param clazz 返回的实体类
     */
    default <T> QueryByIdChain<ID, T> getByIdChain(Class<T> clazz) {
        return new QueryByIdChain<>(this, clazz);
    }

    /**
     * 按照ID查询数据
     */
    default QueryByIdChain<ID, Map<String, Object>> getByIdChain() {
        return new QueryByIdChain<>(this);
    }

    /**
     * 按照ID查询数据
     *
     * @param id    数据ID
     * @param clazz 返回的实体类
     */
    default <T> T getById(@NonNull ID id, Class<T> clazz) {
        return getByIdChain(clazz).id(id).exec();
    }

    /**
     * 按照ID查询数据
     *
     * @param id 数据ID
     */
    default Map<String, Object> getById(@NonNull ID id) {
        return getByIdChain().id(id).exec();
    }

    /**
     * 按照ID列表查询数据
     *
     * @param clazz 返回的实体类
     */
    default <T> QueryByIdsChain<ID, T> getByIdsChain(Class<T> clazz) {
        return new QueryByIdsChain<>(this, clazz);
    }

    /**
     * 按照ID列表查询数据，返回List<Map<String, Object>>类型数据结构
     */
    default QueryByIdsChain<ID, Map<String, Object>> getByIdsChain() {
        return new QueryByIdsChain<>(this);
    }

    /**
     * 按照ID列表查询数据
     *
     * @param ids   数据ID列表
     * @param clazz 返回的实体类
     */
    default <T> List<T> getByIds(@NonNull Collection<ID> ids, Class<T> clazz) {
        return getByIdsChain(clazz).ids(ids).exec();
    }

    /**
     * 按照ID列表查询数据，返回List<Map<String, Object>>类型数据结构
     *
     * @param ids 数据ID列表
     */
    default List<Map<String, Object>> getByIds(@NonNull Collection<ID> ids) {
        return getByIdsChain().ids(ids).exec();
    }

    /**
     * 数据查询，返回List<T>类型数据结构
     *
     * @param clazz 返回的实体类
     **/
    default <T> QueryChain<ID, T> queryChain(Class<T> clazz) {
        return new QueryChain<>(this, clazz);
    }

    /**
     * 数据查询，返回List<Map<String, Object>>类型数据结构
     */
    default QueryChain<ID, Map<String, Object>> queryChain() {
        return new QueryChain<>(this);
    }

    /**
     * 数据查询
     *
     * @param condition 查询条件构建器
     * @return 查询结果列表, List<Map<String, Object>>类型数据结构
     **/
    default List<Map<String, Object>> query(Consumer<ConditionBuilder> condition) {
        return queryChain().where(condition).exec();
    }

    /**
     * 数据查询
     *
     * @param clazz     返回的实体类
     * @param condition 查询条件构建器
     * @return 查询结果列表, List<T>类型数据结构
     **/
    default <T> List<T> query(Class<T> clazz, Consumer<ConditionBuilder> condition) {
        return queryChain(clazz).where(condition).exec();
    }

    /**
     * 查询单条记录
     *
     * @param clazz 返回的实体类
     **/
    default <T> QueryOneChain<ID, T> queryOneChain(Class<T> clazz) {
        return new QueryOneChain<>(this, clazz);
    }

    /**
     * 查询单条记录
     */
    default QueryOneChain<ID, Map<String, Object>> queryOneChain() {
        return new QueryOneChain<>(this);
    }

    /**
     * 查询单条记录
     *
     * @param clazz     返回的实体类
     * @param condition 查询条件构建器
     * @return 查询结果, T类型数据结构
     **/
    default <T> T queryOne(Class<T> clazz, Consumer<ConditionBuilder> condition) {
        return queryOneChain(clazz).where(condition).exec();
    }

    /**
     * 查询单条记录
     *
     * @param condition 查询条件
     * @return 查询结果, Map<String, Object>类型数据结构
     **/
    default Map<String, Object> queryOne(Condition condition) {
        return queryOneChain().where(condition).exec();
    }

    /**
     * 查询单条记录
     *
     * @param condition 查询条件构建器
     * @return 查询结果, Map<String, Object>类型数据结构
     **/
    default Map<String, Object> queryOne(Consumer<ConditionBuilder> condition) {
        return queryOneChain().where(condition).exec();
    }

    /**
     * 返回全部数据
     */
    default List<Map<String, Object>> list() {
        return query(null);
    }

    /**
     * 返回全部数据
     *
     * @param clazz 返回的实体类
     */
    default <T> List<T> list(Class<T> clazz) {
        return query(clazz, null);
    }

    /**
     * 分页查询
     *
     * @param clazz 返回的实体类
     */
    default <T> PageChain<ID, T> pageChain(Class<T> clazz) {
        return new PageChain<>(this, clazz);
    }

    /**
     * 分页查询
     */
    default PageChain<ID, Map<String, Object>> pageChain() {
        return new PageChain<>(this);
    }

    /**
     * 分页查询
     *
     * @param condition   查询条件构建器
     * @param pageCurrent 当前页码
     * @param pageSize    每页数据量
     */
    default PageResult<Map<String, Object>> page(int pageCurrent, int pageSize, Consumer<ConditionBuilder> condition) {
        return pageChain().where(condition).page(pageCurrent, pageSize).exec();
    }

    /**
     * 分页查询
     *
     * @param clazz       返回的实体类
     * @param condition   查询条件构建器
     * @param pageCurrent 当前页码
     * @param pageSize    每页数据量
     */
    default <T> PageResult<T> page(Class<T> clazz, int pageCurrent, int pageSize, Consumer<ConditionBuilder> condition) {
        return pageChain(clazz).where(condition).page(pageCurrent, pageSize).exec();
    }

    /**
     * 流式数据查询，通过回调接口处理返回数据
     **/
    default <T> QueryCallBackChain<ID, T> queryCallBackChain(Class<T> clazz) {
        return new QueryCallBackChain<>(this, clazz);
    }

    /**
     * 流式数据查询，通过回调接口处理返回数据
     */
    default QueryCallBackChain<ID, Map<String, Object>> queryCallBackChain() {
        return new QueryCallBackChain<>(this);
    }

    /**
     * 流式数据查询，通过回调接口处理返回数据
     *
     * @param condition 查询条件构建器
     * @param handler   回调接口
     */
    default int queryCallBack(Consumer<ConditionBuilder> condition, ResultHandler<Map<String, Object>> handler) {
        return queryCallBackChain().where(condition).handler(handler).exec();
    }

    /**
     * 流式数据查询，通过回调接口处理返回数据
     *
     * @param condition 查询条件构建器
     * @param handler   回调接口
     */
    default <T> int queryCallBack(Class<T> clazz, Consumer<ConditionBuilder> condition, ResultHandler<T> handler) {
        return queryCallBackChain(clazz).where(condition).handler(handler).exec();
    }


    /**
     * 流式数据查询，通过回调接口处理返回数据
     **/
    default <T> QueryCursorChain<ID, T> queryCursorChain(Class<T> clazz) {
        return new QueryCursorChain<>(this, clazz);
    }

    /**
     * 流式数据查询，通过回调接口处理返回数据
     */
    default QueryCursorChain<ID, Map<String, Object>> queryCursorChain() {
        return new QueryCursorChain<>(this);
    }

    /**
     * 流式数据查询，通过回调接口处理返回数据
     *
     * @param condition 查询条件构建器
     */
    default QueryCursorChain<ID, Map<String, Object>> queryCursor(Consumer<ConditionBuilder> condition) {
        return queryCursorChain().where(condition);
    }

    /**
     * 流式数据查询，通过回调接口处理返回数据
     *
     * @param condition 查询条件构建器
     */
    default <T> QueryCursorChain<ID, T> queryCursor(Class<T> clazz, Consumer<ConditionBuilder> condition) {
        return queryCursorChain(clazz).where(condition);
    }

    /**
     * 树形递归查询，结果数据通过列表返回
     */
    default <T> QueryRecursiveListChain<ID, T> queryRecursiveListChain(Class<T> clazz) {
        return new QueryRecursiveListChain<>(this, clazz);
    }

    /**
     * 树形递归查询，结果数据通过列表返回
     */
    default QueryRecursiveListChain<ID, Map<String, Object>> queryRecursiveListChain() {
        return new QueryRecursiveListChain<>(this);
    }

    /**
     * 树形递归查询，结果数据通过列表返回
     */
    default <T> List<T> queryRecursiveList(Class<T> clazz, Consumer<ConditionBuilder> initNodeCondition, boolean recursiveDown) {
        return queryRecursiveListChain(clazz).initNodeCondition(initNodeCondition).recursiveDown(recursiveDown).exec();
    }

    /**
     * 树形递归查询，结果数据通过列表返回
     */
    default List<Map<String, Object>> queryRecursiveList(Consumer<ConditionBuilder> initNodeCondition, boolean recursiveDown) {
        return queryRecursiveListChain().initNodeCondition(initNodeCondition).recursiveDown(recursiveDown).exec();
    }

    /**
     * 树形向下递归查询，结果组织为树形结构返回
     */
    default <T> QueryRecursiveTreeChain<ID, T> queryRecursiveTreeChain(Class<T> clazz) {
        return new QueryRecursiveTreeChain<>(this, clazz);
    }

    /**
     * 树形向下递归查询，结果组织为树形结构返回
     */
    default QueryRecursiveTreeChain<ID, Map<String, Object>> queryRecursiveTreeChain() {
        return new QueryRecursiveTreeChain<>(this);
    }

    /**
     * 树形向下递归查询，结果组织为树形结构返回
     */
    default <T> List<T> queryRecursiveTree(Class<T> clazz, Consumer<ConditionBuilder> initNodeCondition) {
        return queryRecursiveTreeChain(clazz).initNodeCondition(initNodeCondition).exec();
    }

    /**
     * 树形向下递归查询，结果组织为树形结构返回
     */
    default List<Map<String, Object>> queryRecursiveTree(Consumer<ConditionBuilder> initNodeCondition) {
        return queryRecursiveTreeChain().initNodeCondition(initNodeCondition).exec();
    }

    /**
     * 树形向下递归查询记录，结果组织为树形结构，并且只有一个根节点
     */
    default <T> QueryOneRecursiveTreeChain<ID, T> queryOneRecursiveTreeChain(Class<T> clazz) {
        return new QueryOneRecursiveTreeChain<>(this, clazz);
    }

    /**
     * 树形向下递归查询记录，结果组织为树形结构，并且只有一个根节点
     */
    default QueryOneRecursiveTreeChain<ID, Map<String, Object>> queryOneRecursiveTreeChain() {
        return new QueryOneRecursiveTreeChain<>(this);
    }

    /**
     * 树形向下递归查询记录，查询指定ID节点和其下子节点数据，结果组织为树形结构
     *
     * @param id    查询根节点ID
     * @param clazz 返回的实体类
     */
    default <T> T getRecursiveTreeById(ID id, Class<T> clazz) {
        return queryOneRecursiveTreeChain(clazz).nullThrowException().initNodeCondition(IdUtil.getIdCondition(getModel(), id)).exec();
    }

    /**
     * 树形向下递归查询记录，查询指定ID节点和其下子节点数据，结果组织为树形结构
     *
     * @param id 查询根节点ID
     */
    default Map<String, Object> getRecursiveTreeById(ID id) {
        return getRecursiveTreeById(id, null);
    }

    /**
     * 树形递归查询统计数据
     */
    default CountRecursiveChain<ID> countRecursiveChain() {
        return new CountRecursiveChain<>(this);
    }

    /**
     * 树形递归查询统计数据
     *
     * @param initNodeCondition 递归查询主表初始条目的查询条件
     * @param recursiveDown     向下递归还是向上递归
     */
    default long countRecursive(Consumer<ConditionBuilder> initNodeCondition, boolean recursiveDown) {
        return countRecursiveChain().initNodeCondition(initNodeCondition).recursiveDown(recursiveDown).exec();
    }

    /**
     * 树形递归查询统计数据
     *
     * @param initNodeCondition 递归查询主表初始条目的查询条件
     * @param condition         递归查询后结果的查询条件，可使用关联表字段条件
     * @param recursiveDown     向下递归还是向上递归
     */
    default long countRecursive(Consumer<ConditionBuilder> initNodeCondition, Consumer<ConditionBuilder> condition, boolean recursiveDown) {
        return countRecursiveChain().initNodeCondition(initNodeCondition).where(condition).recursiveDown(recursiveDown).exec();
    }

    /**
     * 数量查询
     */
    default CountChain<ID> countChain() {
        return new CountChain<>(this);
    }

    /**
     * 数量查询
     */
    default int count() {
        return countChain().exec();
    }

    /**
     * 数量查询
     */
    default int count(Condition condition) {
        return countChain().where(condition).exec();
    }

    /**
     * 数量查询
     */
    default int count(Consumer<ConditionBuilder> condition) {
        return countChain().where(condition).exec();
    }

    default ExistsChain<ID> existsChain() {
        return new ExistsChain<>(this);
    }

    /**
     * 查询是否存在符合条件的记录
     */
    default boolean exists() {
        return existsChain().exec();
    }

    /**
     * 查询是否存在符合条件的记录
     *
     * @param condition 查询条件设置回调函数
     */
    default boolean exists(Consumer<ConditionBuilder> condition) {
        return existsChain().where(condition).exec();
    }

    /**
     * 汇总查询
     **/
    default <T> AggQueryChain<ID, T> aggQueryChain(Class<T> clazz) {
        return new AggQueryChain<>(this, clazz);
    }

    /**
     * 汇总查询
     **/
    default AggQueryChain<ID, Map<String, Object>> aggQueryChain() {
        return new AggQueryChain<>(this);
    }

}
