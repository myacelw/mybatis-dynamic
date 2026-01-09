package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.CustomSelectField;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.SelectColumn;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import io.github.myacelw.mybatis.dynamic.core.service.impl.QueryNode;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据查询执行器
 *
 * @author liuwei
 */
@Slf4j
public class QueryExecution<ID, T> extends AbstractExecution<ID, List<T>, QueryCommand<T>> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return QueryCommand.class;
    }

    @Override
    public List<T> exec(QueryCommand<T> command, DataManager<ID> dataManager) {
        return doQuery(command, dataManager.getModelContext(), root -> root.getSelectColumns(command.isPlain(), command.getClazz() != null, command.getCustomSelectFields()));
    }

    public static <T> List<T> doQuery(QueryCommand<T> command, ModelContext modelContext, Function<QueryNode, List<SelectColumn>> getSelectColumns) {
        return doQuery(command, modelContext, getSelectColumns, null, false, null);
    }

    public static <T> List<T> doQuery(QueryCommand<T> command, ModelContext modelContext, Function<QueryNode, List<SelectColumn>> getSelectColumns, Condition recursiveCondition, boolean recursiveDown, List<String> recursiveFields) {
        Map<String, Object> context = new HashMap<>();
        if (!ObjectUtil.isEmpty(command.getCustomSelectFields())) {
            context.put(CustomSelectField.CONTEXT_KEY, command.getCustomSelectFields());
        }

        QueryNode root = QueryNode.build(modelContext);
        root.addJoins(command.getJoins());
        root.addJoins(command.getCondition(), command.getOrderItems(), command.getCustomSelectFields());
        root.addSelectFields(command.getSelectFields());

        String joinSql = root.getJoinSql(context);
        String whereSql = root.getWhereSql(context, "c", command.getCondition(), recursiveCondition == null, command.isIgnoreLogicDelete());

        List<SelectColumn> columns = getSelectColumns.apply(root);
        String tableAndAs = modelContext.getModel().getSchemaAndTableName() + " AS " + root.getTableAsName();
        String orderBySql = getOrderBySql(command.getOrderItems(), root, context, modelContext.getDialect());

        String sqlPrefix = null;
        if (recursiveCondition != null) {
            sqlPrefix = recursiveSql(modelContext, root.getTableAsName(), recursiveCondition, context, recursiveDown, command.isIgnoreLogicDelete(), recursiveFields);
            tableAndAs = root.getTableAsName();
        }

        Class<T> entityClass = (command.getClazz() == null ? (Class<T>) Map.class : command.getClazz());

        String sql = getQuerySql(context, columns, tableAndAs, joinSql, whereSql, orderBySql, command.getPage(), sqlPrefix);

        log.debug("QUERY SQL: {}, context: {}", sql, context);

        context.put("__sql", sql);
        return modelContext.getMybatisHelper().queryList(modelContext.getSqlSession(), "${__sql}", context, columns, entityClass);
    }

    /**
     * 递归查询SQL
     */
    public static String recursiveSql(ModelContext modelContext, String tableAsName, Condition recursiveCondition, Map<String, Object> context, boolean down, boolean ignoreLogicDelete, List<String> recursiveFields) {
        BasicField[] parentIdFields = modelContext.getModel().getParentIdFields();
        Condition additionalCondition = ignoreLogicDelete ? modelContext.getAdditionalIgnoreDeleteCondition() : modelContext.getAdditionalCondition();

        Condition condition = additionalCondition == null ? recursiveCondition : GroupCondition.and(additionalCondition, recursiveCondition);
        String whereSql = condition == null ? "" : condition.sql("r" + tableAsName, t -> convertColumnForRightField(modelContext, t), modelContext.getDialect());
        context.put("r" + tableAsName, condition);

        String whereSql2 = additionalCondition == null ? "" : additionalCondition.sql("r" + tableAsName, t -> "t" + "." + convertColumnForRightField(modelContext, t), modelContext.getDialect());

        String[] idColumns = convertColumnForAllField(modelContext, modelContext.getModel().getPrimaryKeyFields());
        Assert.isTrue(idColumns.length == parentIdFields.length, "idColumns.length != parentIdFields.length");

        String sql = "SELECT " + getRecursiveSelectColumnSql("t", modelContext, parentIdFields, recursiveFields) + " FROM " + modelContext.getModel().getSchemaAndTableName() + " t";

        StringBuilder joinWhere = new StringBuilder();
        if (down) {
            for (int i = 0; i < idColumns.length; i++) {
                joinWhere.append("t.").append(parentIdFields[i].getColumnName()).append(" = ").append(tableAsName).append(".").append(idColumns[i]);
                if (i != idColumns.length - 1) {
                    joinWhere.append(" AND ");
                }
            }
        } else {
            for (int i = 0; i < idColumns.length; i++) {
                joinWhere.append("t.").append(idColumns[i]).append(" = ").append(tableAsName).append(".").append(parentIdFields[i].getColumnName());
                if (i != idColumns.length - 1) {
                    joinWhere.append(" AND ");
                }
            }
        }

        return "WITH RECURSIVE " + tableAsName + "(" + getRecursiveSelectColumnSql(null, modelContext, parentIdFields, recursiveFields) + ") AS ( " +
                sql + " WHERE " + whereSql +
                " UNION ALL " +
                sql + " JOIN " + tableAsName + " ON " +
                (StringUtil.hasText(whereSql2) ? whereSql2 + " AND " : "") +
                joinWhere +
                ")";
    }

    private static String getRecursiveSelectColumnSql(String columnPrefix, ModelContext modelContext, BasicField[] parentIdFields, List<String> recursiveFields) {
        Set<String> pkOrFkSet = new HashSet<>(Arrays.asList(modelContext.getModel().getPrimaryKeyFields()));
        for (BasicField parentIdField : parentIdFields) {
            pkOrFkSet.add(parentIdField.getColumnName());
        }

        return modelContext.getPermissionFields().stream()
                .filter(t -> t instanceof BasicField).map(t -> (BasicField) t)
                .filter(t -> ObjectUtil.isEmpty(recursiveFields) || recursiveFields.contains(t.getName()) || pkOrFkSet.contains(t.getName()))
                .map(t -> getFullColumnName(columnPrefix, t.getColumnName()))
                .collect(Collectors.joining(", "));
    }

    private static String convertColumnForRightField(ModelContext modelContext, String fieldName) {
        modelContext.getPermissionedField(fieldName, true);
        return convertColumnForAllField(modelContext, fieldName);
    }


    public static String getQuerySql(Map<String, Object> context, List<SelectColumn> columns, String tableAndAs, String joinSql, String whereSql, String orderBySql, Page page, String sqlPrefix) {
        String groupBySql = getGroupBySql(null, columns);

        //如果有join部分 select 列增加表名前缀
        String select = getSelectColumnSql(null, null, columns);

        String sql = (sqlPrefix != null ? sqlPrefix + " " : "") + "SELECT " + select + " FROM " + tableAndAs + (StringUtil.hasText(joinSql) ? " " + joinSql : "");
        if (StringUtil.hasText(whereSql)) {
            sql = sql + " WHERE " + whereSql;
        }
        if (StringUtil.hasText(groupBySql)) {
            sql = sql + " GROUP BY " + groupBySql;
        }
        if (StringUtil.hasText(orderBySql)) {
            sql = sql + " ORDER BY " + orderBySql;
        }

        if (page != null && page.getSize() > 0) {
            int rows = page.getSize();
            int offset = page.getSize() * (page.getCurrent() - 1);
            if (offset > 0) {
                sql = sql + " LIMIT #{_offset}, #{_rows}";
            } else {
                sql = sql + " LIMIT #{_rows}";
            }
            context.put("_rows", rows);
            context.put("_offset", offset);
        }
        return sql;
    }

    private static String getSelectColumnSql(String parentProperty, String columnPrefix, List<SelectColumn> columns) {
        if (ObjectUtil.isEmpty(columns)) {
            return "*";
        } else {
            return columns.stream().map(t -> {
                String property = (StringUtil.hasText(parentProperty) ? parentProperty + "." : "") + t.getProperty();
                if (t.getType() == SelectColumn.Type.ASSOCIATION || t.getType() == SelectColumn.Type.COLLECTION) {
                    return getSelectColumnSql(property, t.getColumnPrefix(), t.getComposites());
                } else {
                    String s = getFullColumnName(columnPrefix, getFullColumnName(t.getColumnPrefix(), t.getColumn()));
                    return s + " AS \"" + property + "\"";
                }
            }).collect(Collectors.joining(", "));
        }
    }

    private static String getGroupBySql(String columnPrefix, List<SelectColumn> columns) {
        if (ObjectUtil.isEmpty(columns)) {
            return null;
        } else {
            return columns.stream().filter(SelectColumn::isGroupBy).map(t -> {
                if (t.getType() == SelectColumn.Type.ASSOCIATION || t.getType() == SelectColumn.Type.COLLECTION) {
                    return getGroupBySql(t.getColumnPrefix(), t.getComposites());
                } else {
                    return getFullColumnName(columnPrefix, getFullColumnName(t.getColumnPrefix(), t.getColumn()));
                }
            }).filter(Objects::nonNull).collect(Collectors.joining(", "));
        }
    }

    public static String getOrderBySql(List<OrderItem> orderItems, QueryNode root, Map<String, Object> context, DataBaseDialect dialect) {
        if (!ObjectUtil.isEmpty(orderItems)) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            int i = 0;
            for (OrderItem orderItem : orderItems) {
                String key = null;
                if (orderItem.getFunctionTemplate() != null && orderItem.getFunctionTemplate().contains("EXPR")) {
                    key = ("o" + i++);
                    context.put(key, orderItem.getFunctionValue());
                }
                String park = orderItem.sql(root.getFieldToSqlConverter(true, context), key, dialect);

                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(park);
            }
            return sb.toString();
        }
        return null;
    }

    private static String getFullColumnName(String columnPrefix, String column) {
        return (StringUtil.hasText(columnPrefix) ? columnPrefix + "." : "") + column;
    }

}
