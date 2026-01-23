package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.CustomSelectField;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.SelectColumn;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCallBackCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import io.github.myacelw.mybatis.dynamic.core.service.impl.QueryNode;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import org.apache.ibatis.session.ResultContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 查询回调执行器
 * <p>
 * 注意：
 * MyBatis 的 Cursor 在某些情况下确实可以聚合 collection。 但这个行为是有条件的，且存在一个“陷阱”。
 * <p>
 * Cursor 的行为取决于数据的顺序。
 * <p>
 * 在 DefaultResultSetHandler.handleRowValuesForNestedResultMap 的代码实现中，MyBatis 在处理嵌套映射时会维护一个 nestedResultObjects 缓存（其实就是当前正在处理的主对象的引用）。
 * <p>
 * 如果数据是有序的： 当 Cursor 读取下一行时，如果 id 相同，它会发现主记录已经在缓存中，于是将新的一行数据加入到已存在的 collection 中，而不产生新的对象。
 * <p>
 * 这种情况下： 在 iterator.next() 时，拿到的那个对象是完整的。
 * <p>
 * 2. 关键陷阱：如果数据无序会发生什么？
 * 这是 Cursor 与 List 查询最本质的区别。
 * <p>
 * List 查询： MyBatis 会先扫描所有行，在内存中完成所有的聚合，最后才把结果给你。即使 SQL 返回的数据是乱序的（例如 ID 为 1, 2, 1），List 最终也只会包含两个对象，ID 为 1 的对象会自动收集两条子记录。
 * <p>
 * Cursor 查询： 为了节省内存，Cursor 是边读边给。当它读取到 ID=1，接着读到 ID=2 时，它会认为 ID=1 的记录已经“结束”了，从而触发结果交付。如果后面再出现一条 ID=1 的记录，Cursor 无法回头去修改已经交给你的那个 ID=1 的对象，它会重新创建一个新的 ID=1 的对象。
 *
 * @author liuwei
 */
public class QueryCallBackExecution<ID, T> extends AbstractExecution<ID, Integer, QueryCallBackCommand<T>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return QueryCallBackCommand.class;
    }

    @Override
    public Integer exec(QueryCallBackCommand<T> command, DataManager<ID> dataManager) {
        Assert.notNull(command.getHandler(), "参数handler不能为空");
        ModelContext modelContext = dataManager.getModelContext();

        Map<String, Object> context = new HashMap<>();
        if (!ObjectUtil.isEmpty(command.getCustomSelectFields())) {
            context.put(CustomSelectField.CONTEXT_KEY, command.getCustomSelectFields());
        }

        QueryNode root = QueryNode.build(modelContext);
        root.addJoins(command.getJoins());
        root.addJoins(command.getCondition(), command.getOrderItems(), command.getCustomSelectFields());
        root.addSelectFields(command.getSelectFields());

        String joinSql = root.getJoinSql(context);
        String whereSql = root.getWhereSql(context, "c", command.getCondition(), true, command.isIgnoreLogicDelete());

        List<SelectColumn> columns = root.getSelectColumns(false, command.getClazz() != null, command.getCustomSelectFields());
        String tableAndAs = modelContext.getModel().getSchemaAndTableName() + " AS " + root.getTableAsName();
        String orderBySql = QueryExecution.getOrderBySql(command.getOrderItems(), root, context, modelContext.getDialect());

        String sql = QueryExecution.getQuerySql(context, columns, tableAndAs, joinSql, whereSql, orderBySql, command.getLimit(), command.getOffset(), null);

        context.put("__sql", sql);

        Class clazz = (command.getClazz() == null ? Map.class : command.getClazz());

        AtomicInteger count = new AtomicInteger(0);

        modelContext.getMybatisHelper().queryCallBack(modelContext.getSqlSession(), "${__sql}", context, columns, clazz, t -> {
            command.getHandler().handleResult((ResultContext) t);
            count.incrementAndGet();
        });

        return count.get();
    }


}
