package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.CustomSelectField;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.SelectColumn;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCursorCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import io.github.myacelw.mybatis.dynamic.core.service.impl.QueryNode;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import org.apache.ibatis.cursor.Cursor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游标查询执行器
 *
 * @author liuwei
 */
public class QueryCursorExecution<ID, T> extends AbstractExecution<ID, Cursor<T>, QueryCursorCommand<T>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return QueryCursorCommand.class;
    }

    @Override
    public Cursor<T> exec(QueryCursorCommand<T> command, DataManager<ID> dataManager) {
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

        List<SelectColumn> columns = root.getSelectColumns(true, command.getClazz() != null, command.getCustomSelectFields());
        String tableAndAs = modelContext.getModel().getSchemaAndTableName() + " AS " + root.getTableAsName();
        String orderBySql = QueryExecution.getOrderBySql(command.getOrderItems(), root, context, modelContext.getDialect());

        String sql = QueryExecution.getQuerySql(context, columns, tableAndAs, joinSql, whereSql, orderBySql, command.getPage(), null);

        context.put("__sql", sql);

        Class clazz = (command.getClazz() == null ? Map.class : command.getClazz());

        return modelContext.getMybatisHelper().queryCursor(modelContext.getSqlSession(), "${__sql}", context, columns, clazz);
    }


}
