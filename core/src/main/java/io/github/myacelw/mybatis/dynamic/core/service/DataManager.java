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
     * 插入单条数据。
     * <p>
     * 该操作会将提供的数据对象或Map持久化到数据库中。
     * 如果模型定义了主键生成策略且主键值为空，框架将自动生成并填充主键。
     *
     * @param data 要插入的数据对象，可以是实体类、Map或包含相同字段的DTO。
     * @return 插入成功后数据的ID。
     */
    default ID insert(@NonNull Object data) {
        return execCommand(new InsertCommand(data, false));
    }

    /**
     * 使用自定义ID插入单条数据，并禁用自动主键生成。
     * <p>
     * 该方法直接使用 {@code data} 中设置的主键值进行插入，忽略模型定义中的任何自动主键生成逻辑。
     *
     * @param data 要插入的数据对象，主键字段必须已赋值。
     * @return 插入的数据ID（即传入的主键值）。
     */
    default ID insertDisableGenerateId(@NonNull Object data) {
        return execCommand(new InsertCommand(data, true));
    }

    /**
     * 批量插入多条数据。
     * <p>
     * 框架会尝试使用数据库特有的批量插入语法（如 MySQL 的单条 INSERT 多值语法）来提高性能。
     *
     * @param data 要插入的数据列表，列表元素可以是实体对象或Map。
     * @return 插入成功后数据的ID列表。
     */
    default List<ID> batchInsert(@NonNull List<?> data) {
        BatchInsertCommand command = new BatchInsertCommand();
        command.setData(data);
        return execCommand(command);
    }

    /**
     * 获取更新操作的处理链。
     * <p>
     * 通过该链式API，可以更灵活地配置更新参数，如强制更新、只更新非空字段等。
     *
     * @return UpdateChain 实例。
     */
    default UpdateChain<ID> updateChain() {
        return new UpdateChain<>(this);
    }

    /**
     * 根据指定的ID更新数据。
     * <p>
     * 该操作会对比传入的 {@code data} 与数据库中现有数据，默认只更新发生变化的字段。
     *
     * @param id   要更新的数据记录的唯一标识。
     * @param data 包含新值的对象或Map。
     */
    default void update(@NonNull ID id, @NonNull Object data) {
        updateChain().id(id).data(data).exec();
    }

    /**
     * 更新数据。
     * <p>
     * ID将自动从 {@code data} 对象中提取。
     * 默认情况下，框架会先查询旧数据，仅对发生变化的字段执行数据库更新。
     *
     * @param data 包含新值及ID的数据对象。
     */
    default void update(@NonNull Object data) {
        updateChain().data(data).exec();
    }

    /**
     * 更新数据中所有非空字段。
     * <p>
     * ID将自动从 {@code data} 对象中提取。
     * 传入对象中为 {@code null} 的字段将被忽略，不会更新到数据库。
     *
     * @param data 包含新值及ID的数据对象。
     */
    default void updateOnlyNonNull(@NonNull Object data) {
        updateChain().data(data).updateOnlyNonNull().exec();
    }

    /**
     * 获取按条件更新操作的处理链。
     * <p>
     * 允许基于复杂条件批量更新数据字段。
     *
     * @return UpdateByConditionChain 实例。
     */
    default UpdateByConditionChain<ID> updateByConditionChain() {
        return new UpdateByConditionChain<>(this);
    }

    /**
     * 根据特定条件更新数据。
     *
     * @param condition         更新条件的构建器回调。
     * @param data              包含新值的对象或Map。
     * @param updateOnlyNonNull 如果为 {@code true}，则跳过 {@code data} 中为 {@code null} 的字段。
     * @return 数据库中受影响的记录条数。
     */
    default int updateByCondition(@NonNull Consumer<ConditionBuilder> condition, Object data, boolean updateOnlyNonNull) {
        return updateByConditionChain().where(condition).data(data).updateOnlyNonNull(updateOnlyNonNull).exec();
    }

    /**
     * 批量更新多条数据。
     * <p>
     * 每个元素必须包含ID。框架将根据ID分别执行更新。
     *
     * @param data 要批量更新的数据列表。
     */
    default void batchUpdate(@NonNull List<?> data) {
        BatchUpdateCommand command = new BatchUpdateCommand();
        command.setData(data);
        execCommand(command);
    }

    /**
     * 批量更新多条数据中的非空字段。
     *
     * @param data 要批量更新的数据列表。
     */
    default void batchUpdateNonNull(@NonNull List<?> data) {
        BatchUpdateCommand command = new BatchUpdateCommand();
        command.setData(data);
        command.setUpdateOnlyNonNull(true);
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
     * 按条件批量更新
     */
    default BatchUpdateByConditionChain<ID> batchUpdateByConditionChain() {
        return new BatchUpdateByConditionChain<>(this);
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
     * 根据ID删除数据。
     * <p>
     * 如果模型启用了逻辑删除，该操作将更新删除标记字段；否则，执行物理删除。
     *
     * @param id 要删除的数据ID。
     * @return 如果删除（或标记删除）成功，返回 {@code true}。
     */
    default boolean delete(@NonNull ID id) {
        int n = execCommand(new DeleteCommand<>(Collections.singletonList(id)));
        return n > 0;
    }

    /**
     * 根据ID删除数据，并支持强制物理删除。
     *
     * @param id                  要删除的数据ID。
     * @param forcePhysicalDelete 如果为 {@code true}，即使模型定义了逻辑删除字段，也将执行物理删除操作。
     * @return 如果删除成功，返回 {@code true}。
     */
    default boolean delete(@NonNull ID id, boolean forcePhysicalDelete) {
        DeleteCommand<ID> command = new DeleteCommand<>(Collections.singletonList(id));
        command.setForcePhysicalDelete(forcePhysicalDelete);
        int n = execCommand(command);
        return n > 0;
    }

    /**
     * 根据条件批量删除数据。
     * <p>
     * 符合条件的记录将按照模型的逻辑删除设置执行删除操作。
     *
     * @param condition 删除条件的构建器回调。
     * @return 被删除或标记删除的记录条数。
     */
    default int delete(Consumer<ConditionBuilder> condition) {
        return delete(condition, false);
    }

    /**
     * 根据条件批量删除数据。
     *
     * @param condition 预定义的 Condition 对象。
     * @return 被删除或标记删除的记录条数。
     */
    default int delete(Condition condition) {
        return delete(condition, false);
    }


    /**
     * 根据条件批量删除数据，并支持强制物理删除。
     *
     * @param condition           删除条件的构建器回调。
     * @param forcePhysicalDelete 如果为 {@code true}，执行物理删除。
     * @return 被删除的记录条数。
     */
    default int delete(Consumer<ConditionBuilder> condition, boolean forcePhysicalDelete) {
        ConditionBuilder conditionBuilder = new ConditionBuilder();
        condition.accept(conditionBuilder);
        return execCommand(new DeleteByConditionCommand(conditionBuilder.build(), forcePhysicalDelete));
    }

    /**
     * 根据条件批量删除数据，并支持强制物理删除。
     *
     * @param condition           预定义的 Condition 对象。
     * @param forcePhysicalDelete 如果为 {@code true}，执行物理删除。
     * @return 被删除的记录条数。
     */
    default int delete(Condition condition, boolean forcePhysicalDelete) {
        return execCommand(new DeleteByConditionCommand(condition, forcePhysicalDelete));
    }

    /**
     * 根据ID列表批量删除数据。
     *
     * @param idList 要删除的ID集合。
     * @return 被删除的记录条数。
     */
    default int batchDelete(@NonNull Collection<ID> idList) {
        return batchDelete(idList, false);
    }

    /**
     * 根据ID列表批量删除数据，并支持强制物理删除。
     *
     * @param idList              要删除的ID集合。
     * @param forcePhysicalDelete 如果为 {@code true}，执行物理删除。
     * @return 被删除的记录条数。
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
     * 根据ID查询单条数据。
     *
     * @param id    数据唯一标识。
     * @param clazz 返回结果转换的目标实体类。
     * @param <T>   实体类类型。
     * @return 查询到的实体对象，若不存在则返回 {@code null}。
     */
    default <T> T getById(@NonNull ID id, Class<T> clazz) {
        return getByIdChain(clazz).id(id).exec();
    }

    /**
     * 根据ID查询单条数据，返回 Map 结构。
     *
     * @param id 数据唯一标识。
     * @return 包含数据字段的 Map 对象，若不存在则返回 {@code null}。
     */
    default Map<String, Object> getById(@NonNull ID id) {
        return getByIdChain().id(id).exec();
    }

    /**
     * 获取按ID查询列表操作的处理链。
     *
     * @param clazz 返回结果转换的目标实体类。
     * @param <T>   实体类类型。
     * @return QueryByIdsChain 实例。
     */
    default <T> QueryByIdsChain<ID, T> getByIdsChain(Class<T> clazz) {
        return new QueryByIdsChain<>(this, clazz);
    }

    /**
     * 获取按ID查询列表操作的处理链，返回 Map 结构。
     *
     * @return QueryByIdsChain 实例。
     */
    default QueryByIdsChain<ID, Map<String, Object>> getByIdsChain() {
        return new QueryByIdsChain<>(this);
    }

    /**
     * 根据ID列表查询多条数据。
     *
     * @param ids   ID集合。
     * @param clazz 返回结果转换的目标实体类。
     * @param <T>   实体类类型。
     * @return 实体对象列表。
     */
    default <T> List<T> getByIds(@NonNull Collection<ID> ids, Class<T> clazz) {
        return getByIdsChain(clazz).ids(ids).exec();
    }

    /**
     * 根据ID列表查询多条数据，返回 Map 列表。
     *
     * @param ids ID集合。
     * @return 包含数据的 Map 列表。
     */
    default List<Map<String, Object>> getByIds(@NonNull Collection<ID> ids) {
        return getByIdsChain().ids(ids).exec();
    }

    /**
     * 获取普通查询操作的处理链。
     *
     * @param clazz 返回结果转换的目标实体类。
     * @param <T>   实体类类型。
     * @return QueryChain 实例。
     **/
    default <T> QueryChain<ID, T> queryChain(Class<T> clazz) {
        return new QueryChain<>(this, clazz);
    }

    /**
     * 获取普通查询操作的处理链，返回 Map 结构。
     *
     * @return QueryChain 实例。
     */
    default QueryChain<ID, Map<String, Object>> queryChain() {
        return new QueryChain<>(this);
    }

    /**
     * 根据条件查询数据列表。
     *
     * @param condition 查询条件构建器回调。
     * @return 包含数据的 Map 列表。
     **/
    default List<Map<String, Object>> query(Consumer<ConditionBuilder> condition) {
        return queryChain().where(condition).exec();
    }

    /**
     * 根据条件查询数据列表，并转换为指定实体类。
     *
     * @param clazz     目标实体类。
     * @param condition 查询条件构建器回调。
     * @param <T>       实体类类型。
     * @return 实体对象列表。
     **/
    default <T> List<T> query(Class<T> clazz, Consumer<ConditionBuilder> condition) {
        return queryChain(clazz).where(condition).exec();
    }

    /**
     * 获取查询单条记录操作的处理链。
     *
     * @param clazz 返回结果转换的目标实体类。
     * @param <T>   实体类类型。
     * @return QueryOneChain 实例。
     **/
    default <T> QueryOneChain<ID, T> queryOneChain(Class<T> clazz) {
        return new QueryOneChain<>(this, clazz);
    }

    /**
     * 获取查询单条记录操作的处理链，返回 Map 结构。
     *
     * @return QueryOneChain 实例。
     */
    default QueryOneChain<ID, Map<String, Object>> queryOneChain() {
        return new QueryOneChain<>(this);
    }

    /**
     * 根据条件查询单条记录。
     *
     * @param clazz     目标实体类。
     * @param condition 查询条件构建器回调。
     * @param <T>       实体类类型。
     * @return 实体对象，若不存在则返回 {@code null}。
     **/
    default <T> T queryOne(Class<T> clazz, Consumer<ConditionBuilder> condition) {
        return queryOneChain(clazz).where(condition).exec();
    }

    /**
     * 根据条件查询单条记录，返回 Map 结构。
     *
     * @param condition 预定义的 Condition 对象。
     * @return 数据 Map，若不存在则返回 {@code null}。
     **/
    default Map<String, Object> queryOne(Condition condition) {
        return queryOneChain().where(condition).exec();
    }

    /**
     * 根据条件查询单条记录，返回 Map 结构。
     *
     * @param condition 查询条件构建器回调。
     * @return 数据 Map，若不存在则返回 {@code null}。
     **/
    default Map<String, Object> queryOne(Consumer<ConditionBuilder> condition) {
        return queryOneChain().where(condition).exec();
    }

    /**
     * 查询并返回当前模型的全部记录条目（Map格式）。
     *
     * @return 所有数据的 Map 列表。
     */
    default List<Map<String, Object>> list() {
        return query(null);
    }

    /**
     * 查询并返回当前模型的全部记录条目，并转换为指定实体类。
     *
     * @param clazz 目标实体类。
     * @param <T>   实体类类型。
     * @return 实体对象列表。
     */
    default <T> List<T> list(Class<T> clazz) {
        return query(clazz, null);
    }

    /**
     * 获取分页查询操作的处理链。
     *
     * @param clazz 返回结果转换的目标实体类。
     * @param <T>   实体类类型。
     * @return PageChain 实例。
     */
    default <T> PageChain<ID, T> pageChain(Class<T> clazz) {
        return new PageChain<>(this, clazz);
    }

    /**
     * 获取分页查询操作的处理链，返回 Map 结构。
     *
     * @return PageChain 实例。
     */
    default PageChain<ID, Map<String, Object>> pageChain() {
        return new PageChain<>(this);
    }

    /**
     * 执行分页查询。
     *
     * @param pageCurrent 当前页码（从1开始）。
     * @param pageSize    每页记录条数。
     * @param condition   查询条件构建器回调。
     * @return 包含数据列表及分页元数据的 PageResult 对象。
     */
    default PageResult<Map<String, Object>> page(int pageCurrent, int pageSize, Consumer<ConditionBuilder> condition) {
        return pageChain().where(condition).page(pageCurrent, pageSize).exec();
    }

    /**
     * 执行分页查询，并转换为指定实体类。
     *
     * @param clazz       目标实体类。
     * @param pageCurrent 当前页码（从1开始）。
     * @param pageSize    每页记录条数。
     * @param condition   查询条件构建器回调。
     * @param <T>         实体类类型。
     * @return 包含数据列表及分页元数据的 PageResult 对象。
     */
    default <T> PageResult<T> page(Class<T> clazz, int pageCurrent, int pageSize, Consumer<ConditionBuilder> condition) {
        return pageChain(clazz).where(condition).page(pageCurrent, pageSize).exec();
    }

    /**
     * 获取查询结果回调处理操作的处理链。
     * <p>
     * 适用于大数据量处理场景，查询结果将逐条或分批通过 {@link ResultHandler} 处理，以节省内存。
     *
     * @param clazz 结果对象转换的目标实体类。
     * @param <T>   数据类型。
     * @return QueryCallBackChain 实例。
     **/
    default <T> QueryCallBackChain<ID, T> queryCallBackChain(Class<T> clazz) {
        return new QueryCallBackChain<>(this, clazz);
    }

    /**
     * 获取查询结果回调处理操作的处理链，使用 Map 结构。
     *
     * @return QueryCallBackChain 实例。
     */
    default QueryCallBackChain<ID, Map<String, Object>> queryCallBackChain() {
        return new QueryCallBackChain<>(this);
    }

    /**
     * 执行带回调的查询。
     *
     * @param condition 查询条件构建器回调。
     * @param handler   处理每条结果的回调接口。
     * @return 被处理的总记录条数。
     */
    default int queryCallBack(Consumer<ConditionBuilder> condition, ResultHandler<Map<String, Object>> handler) {
        return queryCallBackChain().where(condition).handler(handler).exec();
    }

    /**
     * 执行带回调的查询，并转换为指定实体类。
     *
     * @param clazz     目标实体类。
     * @param condition 查询条件构建器回调。
     * @param handler   处理每条结果的回调接口。
     * @param <T>       实体类类型。
     * @return 被处理的总记录条数。
     */
    default <T> int queryCallBack(Class<T> clazz, Consumer<ConditionBuilder> condition, ResultHandler<T> handler) {
        return queryCallBackChain(clazz).where(condition).handler(handler).exec();
    }


    /**
     * 获取流式查询操作的处理链。
     * <p>
     * 返回 MyBatis 的 {@link Cursor}，允许遍历结果集时延迟加载，适用于大数据量流式处理。
     *
     * @param clazz 结果对象转换的目标实体类。
     * @param <T>   数据类型。
     * @return QueryCursorChain 实例。
     **/
    default <T> QueryCursorChain<ID, T> queryCursorChain(Class<T> clazz) {
        return new QueryCursorChain<>(this, clazz);
    }

    /**
     * 获取流式查询操作的处理链，使用 Map 结构。
     *
     * @return QueryCursorChain 实例。
     */
    default QueryCursorChain<ID, Map<String, Object>> queryCursorChain() {
        return new QueryCursorChain<>(this);
    }

    /**
     * 根据条件开启流式查询。
     *
     * @param condition 查询条件构建器回调。
     * @return 包含查询结果的可遍历 Cursor 实例。
     */
    default QueryCursorChain<ID, Map<String, Object>> queryCursor(Consumer<ConditionBuilder> condition) {
        return queryCursorChain().where(condition);
    }

    /**
     * 根据条件开启流式查询，并转换为指定实体类。
     *
     * @param clazz     目标实体类。
     * @param condition 查询条件构建器回调。
     * @param <T>       实体类类型。
     * @return 包含转换后对象的可遍历 Cursor 实例。
     */
    default <T> QueryCursorChain<ID, T> queryCursor(Class<T> clazz, Consumer<ConditionBuilder> condition) {
        return queryCursorChain(clazz).where(condition);
    }

    /**
     * 获取树形递归查询操作的处理链，结果以扁平化列表返回。
     *
     * @param clazz 返回结果转换的目标实体类。
     * @param <T>   实体类类型。
     * @return QueryRecursiveListChain 实例。
     */
    default <T> QueryRecursiveListChain<ID, T> queryRecursiveListChain(Class<T> clazz) {
        return new QueryRecursiveListChain<>(this, clazz);
    }

    /**
     * 获取树形递归查询操作的处理链，结果以扁平化列表返回（Map结构）。
     *
     * @return QueryRecursiveListChain 实例。
     */
    default QueryRecursiveListChain<ID, Map<String, Object>> queryRecursiveListChain() {
        return new QueryRecursiveListChain<>(this);
    }

    /**
     * 执行树形递归查询，结果返回为扁平化列表。
     *
     * @param clazz             目标实体类。
     * @param initNodeCondition 递归起始节点的查询条件。
     * @param recursiveDown     是否向下递归。
     * @param <T>               实体类类型。
     * @return 包含所有层级记录的列表。
     */
    default <T> List<T> queryRecursiveList(Class<T> clazz, Consumer<ConditionBuilder> initNodeCondition, boolean recursiveDown) {
        return queryRecursiveListChain(clazz).initNodeCondition(initNodeCondition).recursiveDown(recursiveDown).exec();
    }

    /**
     * 执行树形递归查询，结果返回为扁平化列表（Map结构）。
     *
     * @param initNodeCondition 递归起始节点的查询条件。
     * @param recursiveDown     是否向下递归。
     * @return 包含所有层级记录的列表。
     */
    default List<Map<String, Object>> queryRecursiveList(Consumer<ConditionBuilder> initNodeCondition, boolean recursiveDown) {
        return queryRecursiveListChain().initNodeCondition(initNodeCondition).recursiveDown(recursiveDown).exec();
    }

    /**
     * 获取树形递归查询操作的处理链，结果以嵌套树结构返回。
     *
     * @param clazz 返回结果转换的目标实体类。
     * @param <T>   实体类类型。
     * @return QueryRecursiveTreeChain 实例。
     */
    default <T> QueryRecursiveTreeChain<ID, T> queryRecursiveTreeChain(Class<T> clazz) {
        return new QueryRecursiveTreeChain<>(this, clazz);
    }

    /**
     * 获取树形递归查询操作的处理链，结果以嵌套树结构返回（Map结构）。
     *
     * @return QueryRecursiveTreeChain 实例。
     */
    default QueryRecursiveTreeChain<ID, Map<String, Object>> queryRecursiveTreeChain() {
        return new QueryRecursiveTreeChain<>(this);
    }

    /**
     * 执行树形递归查询，并将结果组织为嵌套树形结构。
     *
     * @param clazz             目标实体类。
     * @param initNodeCondition 递归起始节点的查询条件。
     * @param <T>               实体类类型。
     * @return 嵌套的树形数据列表。
     */
    default <T> List<T> queryRecursiveTree(Class<T> clazz, Consumer<ConditionBuilder> initNodeCondition) {
        return queryRecursiveTreeChain(clazz).initNodeCondition(initNodeCondition).exec();
    }

    /**
     * 执行树形递归查询，并将结果组织为嵌套树形结构（Map结构）。
     *
     * @param initNodeCondition 递归起始节点的查询条件。
     * @return 嵌套的树形数据列表（Map集合）。
     */
    default List<Map<String, Object>> queryRecursiveTree(Consumer<ConditionBuilder> initNodeCondition) {
        return queryRecursiveTreeChain().initNodeCondition(initNodeCondition).exec();
    }

    /**
     * 获取单一树（指定根节点及其下属节点）查询操作的处理链。
     *
     * @param clazz 返回结果转换的目标实体类。
     * @param <T>   实体类类型。
     * @return QueryOneRecursiveTreeChain 实例。
     */
    default <T> QueryOneRecursiveTreeChain<ID, T> queryOneRecursiveTreeChain(Class<T> clazz) {
        return new QueryOneRecursiveTreeChain<>(this, clazz);
    }

    /**
     * 获取单一树（指定根节点及其下属节点）查询操作的处理链，返回 Map 结构。
     *
     * @return QueryOneRecursiveTreeChain 实例。
     */
    default QueryOneRecursiveTreeChain<ID, Map<String, Object>> queryOneRecursiveTreeChain() {
        return new QueryOneRecursiveTreeChain<>(this);
    }

    /**
     * 查询指定 ID 节点及其所有子孙节点，并组织为树形结构返回。
     *
     * @param id    根节点 ID。
     * @param clazz 目标实体类。
     * @param <T>   实体类类型。
     * @return 嵌套的树形对象。
     */
    default <T> T getRecursiveTreeById(ID id, Class<T> clazz) {
        return queryOneRecursiveTreeChain(clazz).nullThrowException().initNodeCondition(IdUtil.getIdCondition(getModel(), id)).exec();
    }

    /**
     * 查询指定 ID 节点及其所有子孙节点，并组织为树形结构返回（Map 结构）。
     *
     * @param id 根节点 ID。
     * @return 嵌套的树形数据 Map。
     */
    default Map<String, Object> getRecursiveTreeById(ID id) {
        return getRecursiveTreeById(id, null);
    }

    /**
     * 获取树形递归统计操作的处理链。
     *
     * @return CountRecursiveChain 实例。
     */
    default CountRecursiveChain<ID> countRecursiveChain() {
        return new CountRecursiveChain<>(this);
    }

    /**
     * 执行树形递归查询并统计结果条数。
     *
     * @param initNodeCondition 递归起始节点的查询条件。
     * @param recursiveDown     是否向下递归。
     * @return 统计得到的总记录条数。
     */
    default int countRecursive(Consumer<ConditionBuilder> initNodeCondition, boolean recursiveDown) {
        return countRecursiveChain().initNodeCondition(initNodeCondition).recursiveDown(recursiveDown).exec();
    }

    /**
     * 执行树形递归查询并统计符合额外条件的记录条数。
     *
     * @param initNodeCondition 递归起始节点的查询条件。
     * @param condition         递归结果集的二次过滤条件。
     * @param recursiveDown     是否向下递归。
     * @return 统计得到的符合条件的记录条数。
     */
    default int countRecursive(Consumer<ConditionBuilder> initNodeCondition, Consumer<ConditionBuilder> condition, boolean recursiveDown) {
        return countRecursiveChain().initNodeCondition(initNodeCondition).where(condition).recursiveDown(recursiveDown).exec();
    }

    /**
     * 获取统计记录条数操作的处理链。
     *
     * @return CountChain 实例。
     */
    default CountChain<ID> countChain() {
        return new CountChain<>(this);
    }

    /**
     * 统计当前模型的总记录条数。
     *
     * @return 总记录条数。
     */
    default int count() {
        return countChain().exec();
    }

    /**
     * 统计符合条件的记录条数。
     *
     * @param condition 预定义的 Condition 对象。
     * @return 符合条件的记录条数。
     */
    default int count(Condition condition) {
        return countChain().where(condition).exec();
    }

    /**
     * 统计符合条件的记录条数。
     *
     * @param condition 查询条件构建器回调。
     * @return 符合条件的记录条数。
     */
    default int count(Consumer<ConditionBuilder> condition) {
        return countChain().where(condition).exec();
    }

    /**
     * 获取判断记录是否存在操作的处理链。
     *
     * @return ExistsChain 实例。
     */
    default ExistsChain<ID> existsChain() {
        return new ExistsChain<>(this);
    }

    /**
     * 判断当前模型中是否存在任何记录。
     *
     * @return 如果存在至少一条记录，返回 {@code true}。
     */
    default boolean exists() {
        return existsChain().exec();
    }

    /**
     * 判断是否存在符合特定条件的记录。
     *
     * @param condition 查询条件构建器回调。
     * @return 如果存在符合条件的记录，返回 {@code true}。
     */
    default boolean exists(Consumer<ConditionBuilder> condition) {
        return existsChain().where(condition).exec();
    }

    /**
     * 获取汇总查询（聚合查询）操作的处理链。
     *
     * @param clazz 汇总结果转换的目标类。
     * @param <T>   结果类型。
     * @return AggQueryChain 实例。
     **/
    default <T> AggQueryChain<ID, T> aggQuery(Class<T> clazz) {
        return new AggQueryChain<>(this, clazz);
    }

    /**
     * 获取汇总查询（聚合查询）操作的处理链，返回 Map 结构。
     *
     * @return AggQueryChain 实例。
     **/
    default AggQueryChain<ID, Map<String, Object>> aggQuery() {
        return new AggQueryChain<>(this);
    }

}
