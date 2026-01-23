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
