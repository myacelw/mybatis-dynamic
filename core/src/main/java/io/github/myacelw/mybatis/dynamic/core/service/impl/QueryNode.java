package io.github.myacelw.mybatis.dynamic.core.service.impl;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.github.myacelw.mybatis.dynamic.core.exception.SystemException;
import io.github.myacelw.mybatis.dynamic.core.exception.crud.FieldParameterException;
import io.github.myacelw.mybatis.dynamic.core.exception.crud.JoinException;
import io.github.myacelw.mybatis.dynamic.core.exception.crud.JoinFieldException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.*;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.*;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.*;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.*;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.SelectColumn;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import io.github.myacelw.mybatis.dynamic.core.util.tuple.Tuple3;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 数据查询节点。
 * 用于描述关联查询的树形关系。
 * 例如： 模型User 的 address 属性 关联表 Address 的 city属性，city属性 关联表 City，那么可以使用QueryNode的树形关系来描述这种关联关系。
 *
 * @author liuwei
 */
@Slf4j
public class QueryNode {
    public static final String DEFAULT_TABLE_AS_PREFIX = "t";

    /**
     * 通配符字段。
     */
    private static final BasicField WILDCARD_FIELD = new BasicField();

    /**
     * 对应模型的数据管理器，例如：City 的数据管理器。
     */
    ModelContext modelContext;

    AtomicInteger indexCounter;

    /**
     * 表别名索引，例如：t0,t1,t2
     */
    int tableAsIndex;

    /**
     * 查询字段，key为BasicField时，value为空；为GroupField时，value有值。
     */
    Map<Field, Set<BasicField>> selectFields = new HashMap<>();

    /**
     * Join查询设置
     */
    Join join;

    /**
     * 上级模型关联字段，例如：Address 的 city 字段。
     * 查询主表根节点该值为空。
     */
    RefModel linkField;

    /**
     * 上级节点，查询主表根节点该值为空。
     */
    @JsonBackReference
    QueryNode parent;

    @JsonManagedReference
    List<QueryNode> children = new ArrayList<>();

    /**
     * 构建QueryNode
     */
    public static QueryNode build(ModelContext modelContext) {
        AtomicInteger indexCounter = new AtomicInteger(0);
        QueryNode root = new QueryNode();
        root.modelContext = modelContext;
        root.indexCounter = indexCounter;
        root.tableAsIndex = indexCounter.getAndIncrement();
        return root;
    }

    public void addJoins(List<Join> joins) {
        if (joins != null) {
            joins.stream()
                    .sorted(Comparator.comparing(Join::getFieldPath, Comparator.naturalOrder()))
                    .forEach(join -> {
                        String fieldPath = join.getFieldPath();
                        Tuple3<QueryNode, Field, BasicField> tuple = getOrCreateNode(fieldPath, true, true, false);
                        tuple.v1.join = join;
                    });
        }
    }

    /**
     * 从SQL语句各个部分涉及的关联字段自动加入为Join。
     */
    public void addJoins(Condition condition, List<OrderItem> orderItems, List<CustomSelectField> customSelectFields) {
        Set<String> fields = new HashSet<>();
        if (condition != null) {
            fields.addAll(condition.innerGetSimpleConditionFields());
        }
        if (!ObjectUtil.isEmpty(orderItems)) {
            for (OrderItem orderItem : orderItems) {
                if (orderItem.getField() != null) {
                    fields.add(orderItem.getField());
                }
            }
        }
        if (!ObjectUtil.isEmpty(customSelectFields)) {
            for (CustomSelectField customSelectField : customSelectFields) {
                if (customSelectField.getFields() != null) {
                    fields.addAll(customSelectField.getFields());
                }
            }
        }
        if (ObjectUtil.isEmpty(fields)) {
            return;
        }
        fields.stream()
                .sorted(Comparator.naturalOrder())
                .forEach(fieldPath -> getOrCreateNode(fieldPath, true, true, false));
    }

    /**
     * 添加查询Select字段
     */
    public void addSelectFields(List<String> selectFields) {
        // 为空时，添加所有权限字段
        if (selectFields == null) {
            addDefaultSelectFields();
            return;
        }

        Set<String> fields = new HashSet<>(selectFields);
        //加入主键
        String[] primaryKeyFields = modelContext.getModel().getPrimaryKeyFields();
        if (primaryKeyFields != null) {
            Collections.addAll(fields, primaryKeyFields);
        }

        fields.stream()
                .sorted(Comparator.naturalOrder())
                .forEach(fieldPath -> {
                    Tuple3<QueryNode, Field, BasicField> tuple = getOrCreateNode(fieldPath, true, true, true);
                    QueryNode queryNode = tuple.v1;
                    Field field = tuple.v2;
                    BasicField subField = tuple.v3;
                    if (field == null || field == WILDCARD_FIELD) {
                        queryNode.addDefaultSelectFields();
                    } else {
                        queryNode.addSelectField(field, subField);
                    }
                });
    }

    private void addDefaultSelectFields() {
        for (Field field : this.modelContext.getPermissionFields()) {
            addDefaultSelectField(field);
        }
        for (QueryNode child : this.children) {
            child.addSelectFields(null);
        }
    }

    private void addDefaultSelectField(Field field) {
        if (field instanceof BasicField) {
            BasicField basicField = (BasicField) field;
            if (basicField.getSelect() != Boolean.FALSE) {
                selectFields.put(field, null);
            }
        } else if (field instanceof GroupField) {
            GroupField groupField = (GroupField) field;
            if (groupField.getSelect() != Boolean.FALSE) {
                addDefaultSelectFieldForGroupField((GroupField) field);
            }
        }
    }

    private void addDefaultSelectFieldForGroupField(GroupField groupField) {
        Set<BasicField> subFields = groupField.getFields().stream().filter(t -> t.getSelect() != Boolean.FALSE).collect(Collectors.toSet());
        selectFields.put(groupField, subFields);
    }

    private void addSelectField(Field field, BasicField subField) {
        if (field instanceof BasicField) {
            selectFields.put(field, null);
        } else if (field instanceof GroupField) {
            GroupField groupField = (GroupField) field;
            if (subField == null || subField == WILDCARD_FIELD) {
                addDefaultSelectFieldForGroupField(groupField);
            } else {
                selectFields.computeIfAbsent(groupField, k -> new HashSet<>()).add(subField);
            }
        }
    }

    /**
     * 生成Sql Join部分
     */
    public String getJoinSql(Map<String, Object> context) {
        StringBuilder joinSql = new StringBuilder();
        if (this.linkField != null) {
            String joinTableName = this.modelContext.getModel().getSchemaAndTableName();
            String joinTableAsName = this.getTableAsName();
            joinSql.append(" ").append(getJoinType()).append(" JOIN ")
                    .append(joinTableName).append(" AS ").append(joinTableAsName)
                    .append(" ON ").append(getJoinWhereSql(context));
        }
        //递归生成
        for (QueryNode child : this.children) {
            joinSql.append(child.getJoinSql(context));
        }
        return joinSql.toString();
    }

    /**
     * 生成存在条件SQL
     */
    private String getExistsSql(Map<String, Object> context) {
        StringBuilder joinSql = new StringBuilder();

        String joinTableName = this.modelContext.getModel().getSchemaAndTableName();
        String joinTableAsName = this.getTableAsName();

        joinSql.append("SELECT 1 FROM ").append(joinTableName).append(" AS ").append(joinTableAsName);

        //递归生成
        for (QueryNode child : this.children) {
            joinSql.append(child.getJoinSql(context));
        }

        joinSql.append(" WHERE ").append(getJoinWhereSql(context));
        return joinSql.toString();
    }

    private String getJoinWhereSql(Map<String, Object> context) {
        Field field = this.linkField;
        String sql;
        if (field instanceof ToOneField) {
            sql = getJoinToOneWhereSql((ToOneField) field);
        } else if (field instanceof ToManyField) {
            sql = getJoinToManyWhereSql((ToManyField) field);
        } else {
            throw new JoinFieldException("模型[" + this.modelContext.getModel().getName() + "]Join查询字段[" + field.getName() + "]不是ToOneField或ToManyField类型字段");
        }

        String joinConditionSql = getWhereSql(context, "j" + tableAsIndex, join.getCondition(), true, join.isIgnoreLogicDelete());
        if (StringUtil.hasText(joinConditionSql)) {
            return sql + " AND " + joinConditionSql;
        }
        return sql;
    }

    private String getJoinToOneWhereSql(ToOneField field) {
        Model model = this.modelContext.getModel();

        //本节点表
        String[] joinColumns = Arrays.stream(model.getPrimaryKeyFields()).map(t -> model.findBasicField(t).getColumnName()).toArray(String[]::new);

        //上级表别名
        Model joinToModel = this.parent.modelContext.getModel();

        String[] joinToColumns = Arrays.stream(field.getJoinLocalFields()).map(t -> joinToModel.findBasicField(t).getColumnName()).toArray(String[]::new);

        Assert.isTrue(joinColumns.length == joinToColumns.length, "模型[" + joinToModel.getName() + "]的[" + field.getName() + "]字段配置错误，和关联模型[" + model.getName() + "]主键字段数量不一致。");

        return doGetJoinWhereSql(joinColumns, joinToColumns);
    }

    private String getJoinToManyWhereSql(ToManyField field) {
        Model model = this.modelContext.getModel();

        //本节点表
        String[] joinColumns = Arrays.stream(field.getJoinTargetFields()).map(t -> model.findBasicField(t).getColumnName()).toArray(String[]::new);

        //上级表别名
        Model joinToModel = this.parent.modelContext.getModel();
        String[] joinToColumns = Arrays.stream(joinToModel.getPrimaryKeyFields()).map(t -> joinToModel.findBasicField(t).getColumnName()).toArray(String[]::new);

        Assert.isTrue(joinColumns.length == joinToColumns.length, "模型[" + model.getName() + "]的[" + field.getName() + "]字段配置错误，和关联模型[" + model.getName() + "]主键字段数量不一致。");

        return doGetJoinWhereSql(joinColumns, joinToColumns);
    }

    private String doGetJoinWhereSql(String[] joinColumns, String[] joinToColumns) {
        //本节点表
        String joinTableAsName = this.getTableAsName();

        //上级表别名
        String joinTo = this.parent.getTableAsName();

        StringBuilder joinSql = new StringBuilder();

        for (int i = 0; i < joinColumns.length; i++) {
            String joinColumn = joinColumns[i];
            String joinToColumn = joinToColumns[i];
            if (i != 0) {
                joinSql.append(" AND ");
            }
            joinSql.append(joinTableAsName).append(".").append(joinColumn).append("=").append(joinTo).append(".").append(joinToColumn);
        }

        return joinSql.toString();
    }

    public String getWhereSql(Map<String, Object> context, String valueExpression, Condition condition, boolean havaAdditionalCondition, boolean ignoreLogicDelete) {
        String conditionSql1 = null;
        if (havaAdditionalCondition) {
            Condition additionalCondition = ignoreLogicDelete ? modelContext.getAdditionalIgnoreDeleteCondition() : modelContext.getAdditionalCondition();
            conditionSql1 = additionalCondition == null ? "" : additionalCondition.sql(valueExpression + "0", getFieldToSqlConverter(false, context), modelContext.getDialect());
            context.put(valueExpression + "0", additionalCondition);
        }
        if (condition == null) {
            return conditionSql1;
        }

        String conditionSql2 = condition.sql(valueExpression + "1", getFieldToSqlConverter(true, context), modelContext.getDialect());
        context.put(valueExpression + "1", condition);
        return StringUtil.hasText(conditionSql1) ? GroupCondition.and(conditionSql1, conditionSql2) : conditionSql2;
    }

    private Join.JoinType getJoinType() {
        return this.join.getType() == null ? Join.JoinType.LEFT : this.join.getType();
    }

    public List<SelectColumn> getSelectColumns(boolean plain, boolean useFieldJavaType, List<CustomSelectField> customSelectFields) {
        return getSelectColumns(plain, "", useFieldJavaType, customSelectFields);
    }

    private List<SelectColumn> getSelectColumns(boolean plain, String propertyPrefix, boolean useFieldJavaType, List<CustomSelectField> customSelectFields) {
        List<SelectColumn> result = new ArrayList<>();

        for (Map.Entry<Field, Set<BasicField>> entry : selectFields.entrySet()) {
            Field field = entry.getKey();
            Set<BasicField> subFields = entry.getValue();

            if (field.getName().equals(Model.FIELD_DELETE_FLAG) && (join == null || !join.isIgnoreLogicDelete())) {
                continue;
            }
            result.addAll(getSelectColumnsForField(propertyPrefix, field, subFields));
        }

        for (QueryNode child : children) {
            result.addAll(child.getSelectColumnsForRefModel(plain, useFieldJavaType));
        }

        //处理自定义返回列
        if (customSelectFields != null) {
            int i = 0;
            for (CustomSelectField customSelectField : customSelectFields) {
                List<String> columns = new ArrayList<>();
                if (customSelectField.getFields() != null) {
                    for (String field : customSelectField.getFields()) {
                        String column = this.convertFieldToColumn(field, true);
                        columns.add(column);
                    }
                }
                String valueExpression = CustomSelectField.CONTEXT_KEY + "[" + (i++) + "]";
                String sql = Condition.replacePlaceholders(valueExpression, null, customSelectField.getSqlTemplate(), columns.toArray(new String[0]));
                if (!columns.isEmpty()) {
                    sql = Condition.replacePlaceholders(valueExpression, null, customSelectField.getSqlTemplate(), columns.get(0));
                }
                result.add(SelectColumn.column(sql, customSelectField.getName(), customSelectField.getJavaType(), customSelectField.getTypeHandler(), customSelectField.getJdbcType()));
            }
        }

        return result;
    }

    private List<SelectColumn> getSelectColumnsForField(String propertyPrefix, Field field, Set<BasicField> subFields) {
        List<SelectColumn> result = new ArrayList<>();

        if (field instanceof BasicField) {
            BasicField basicField = (BasicField) field;
            String column = getSelectColumn(basicField);
            if (modelContext.getModel().isPrimaryKeyField(field.getName())) {
                result.add(SelectColumn.id(column, propertyPrefix + basicField.getName(), basicField.getJavaClass(), basicField.getTypeHandlerClass(), basicField.getJdbcType()));
            } else {
                result.add(SelectColumn.column(column, propertyPrefix + basicField.getName(), basicField.getJavaClass(), basicField.getJavaParameterClass(), basicField.getTypeHandlerClass(), basicField.getJdbcType()));
            }
        }
        if (field instanceof GroupField) {
            GroupField groupField = (GroupField) field;
            subFields.forEach(subField -> {
                String column = getSelectColumn(subField);
                Class<?> typeHandlerClass = subField.getTypeHandlerClass();
                result.add(SelectColumn.column(column, propertyPrefix + groupField.getName() + "." + subField.getName(), subField.getJavaClass(), subField.getJavaParameterClass(), subField.getTypeHandlerClass(), subField.getJdbcType()));
            });
        }
        return result;
    }

    private List<SelectColumn> getSelectColumnsForRefModel(boolean plain, boolean useFieldJavaType) {
        String fieldName = linkField.getName();
        Class<?> javaClass = linkField.getJavaClass();

        if (plain) {
            return getSelectColumns(true, fieldName + ".", useFieldJavaType, null);
        } else {
            List<SelectColumn> subs = getSelectColumns(false, "", useFieldJavaType, null);
            if (subs.isEmpty()) {
                return Collections.emptyList();
            }
            if (linkField instanceof ToOneField) {
                return Collections.singletonList(SelectColumn.association(null, fieldName, subs, javaClass));
            } else {
                return Collections.singletonList(SelectColumn.collection(null, fieldName, subs, javaClass));
            }
        }
    }

    public List<SelectColumn> getAggSelectColumns(List<AggSelectItem> aggSelectItems) {
        List<SelectColumn> selectColumns = new ArrayList<>();
        for (AggSelectItem item : aggSelectItems) {
            String fieldName = item.getField();
            if (item.getAggFunction() == AggFunction.COUNT && ("1".equals(fieldName) || "*".equals(fieldName))) {
                selectColumns.add(SelectColumn.column("COUNT(*)", item.getPropertyName(), Integer.class, null, null));
            } else if (item.getAggFunction() == null || item.getAggFunction() == AggFunction.NONE) {
                String column = convertFieldToColumn(fieldName, true);
                Tuple3<QueryNode, Field, BasicField> tuple3 = findField(fieldName, true);
                Class<?> javaType = tuple3.v3 != null ? tuple3.v3.getJavaClass() : tuple3.v2.getJavaClass();

                if (item.getCustomFunction() != null) {
                    column = item.getCustomFunction().replaceAll("\\$COL", column);
                    javaType = item.getJavaType();
                }

                SelectColumn sc = SelectColumn.column(column, item.getPropertyName(), javaType, null, null);
                sc.setGroupBy(true);
                selectColumns.add(sc);
            } else {
                String column = item.getAggFunction().toSelectColumn(convertFieldToColumn(fieldName, true), this.modelContext.getDialect());
                Class<?> javaType;
                if (item.getAggFunction() == AggFunction.CUSTOM && item.getCustomFunction() != null) {
                    column = item.getCustomFunction().replaceAll("\\$COL", column);
                    javaType = item.getJavaType();
                } else {
                    Tuple3<QueryNode, Field, BasicField> tuple3 = findField(fieldName, true);
                    if (item.getJavaType() == null) {
                        javaType = tuple3.v3 != null ? tuple3.v3.getJavaClass() : tuple3.v2.getJavaClass();
                    } else {
                        javaType = item.getJavaType();
                    }
                }

                SelectColumn sc = SelectColumn.column(column, item.getPropertyName(), item.getAggFunction().getJavaType(javaType), null, null);
                selectColumns.add(sc);
            }
        }
        return selectColumns;
    }

    private String getSelectColumn(BasicField field) {
        return getTableAsName() + "." + field.getColumnName();
    }

    /**
     * 查找或创建节点
     *
     * @param fieldPath      字段路径， 例如：address.city
     * @param enabledCreate  是否允许创建节点
     * @param havePermission 是否有权限
     * @param allowWildcard  用于支持通配符，存在通配符时会返回固定的WILDCARD_FIELD字段。
     */
    public Tuple3<QueryNode, Field, BasicField> getOrCreateNode(String fieldPath, boolean enabledCreate, boolean havePermission, boolean allowWildcard) {
        int index = fieldPath.indexOf(".");
        if (index == -1) {
            Optional<QueryNode> child = children.stream().filter(t -> fieldPath.equals(t.linkField.getName())).findFirst();
            if (child.isPresent()) {
                return new Tuple3<>(child.get(), null, null);
            } else {
                if (allowWildcard && fieldPath.equals(Field.ALL_WILDCARD)) {
                    return new Tuple3<>(this, WILDCARD_FIELD, null);
                }
                Field field = modelContext.getField(fieldPath, havePermission, true);
                if (field instanceof RefModel) {
                    if (!enabledCreate) {
                        throw new SystemException("系统错误，当前状态不支持创建QueryNode, 模型'" + this.modelContext.getModel().getName() + "' 关联字段'" + fieldPath + "'");
                    }

                    RefModel refModel = (RefModel) field;
                    QueryNode newNode = new QueryNode();
                    newNode.join = Join.of(fieldPath);
                    newNode.parent = this;
                    newNode.linkField = refModel;
                    newNode.modelContext = getRefModelContext(refModel);
                    newNode.indexCounter = this.indexCounter;
                    newNode.tableAsIndex = this.indexCounter.getAndIncrement();

                    children.add(newNode);
                    return new Tuple3<>(newNode, null, null);
                } else {
                    return new Tuple3<>(this, field, null);
                }
            }
        }

        String field = fieldPath.substring(0, index);
        String subFieldPath = fieldPath.substring(index + 1);

        Tuple3<QueryNode, Field, BasicField> next = getOrCreateNode(field, enabledCreate, havePermission, allowWildcard);
        QueryNode child = next.v1;
        Field childField = next.v2;

        Assert.isFalse(childField instanceof BasicField, "模型'" + modelContext.getModel().getName() + "'不存在字段'" + fieldPath + "'");

        if (childField instanceof GroupField) {
            GroupField groupField = (GroupField) childField;
            if (allowWildcard && subFieldPath.equals(Field.ALL_WILDCARD)) {
                return new Tuple3<>(child, childField, WILDCARD_FIELD);
            }

            BasicField basicField = groupField.findField(subFieldPath);
            Assert.notNull(basicField, "模型'" + modelContext.getModel().getName() + "'不存在字段'" + fieldPath + "'");
            return new Tuple3<>(child, childField, basicField);
        }

        return child.getOrCreateNode(subFieldPath, enabledCreate, havePermission, allowWildcard);
    }


    private Tuple3<QueryNode, Field, BasicField> findField(String fieldPath, boolean haveRighted) {
        Tuple3<QueryNode, Field, BasicField> tuple3 = getOrCreateNode(fieldPath, false, haveRighted, false);

        if (tuple3.v2 == null) {
            throw new FieldParameterException("模型[" + modelContext.getModel().getName() + "]的字段[" + fieldPath + "]是关联类型字段，不能作为条件或者返回值");
        }

        return tuple3;
    }

    public FieldToSqlConverter getFieldToSqlConverter(boolean haveRighted, Map<String, Object> context) {
        return new FieldToSqlConverter() {
            @Override
            public String convertColumn(String fieldName) {
                return convertFieldToColumn(fieldName, haveRighted);
            }

            @Override
            public ConvertExistsSqlResult convertExistsSql(String fieldPath, List<Join> joins) {
                Tuple3<QueryNode, Field, BasicField> tuple3 = findField(fieldPath, haveRighted);

                if (tuple3.v3 != null) {
                    throw new FieldParameterException("模型[" + modelContext.getModel().getName() + "]的字段[" + fieldPath + "]" + "不是关联关系类型，不能进行Exists查询");
                }
                QueryNode queryNode = tuple3.v1;
                Field field = tuple3.v2;

                Assert.isTrue(field instanceof RefModel, "模型[" + modelContext.getModel().getName() + "]的字段[" + fieldPath + "]" + "不是关联关系类型，不能进行Exists查询");

                QueryNode existsQueryNode = new QueryNode();
                existsQueryNode.modelContext = getRefModelContext((RefModel) field);
                existsQueryNode.indexCounter = indexCounter;
                existsQueryNode.tableAsIndex = indexCounter.getAndIncrement();
                existsQueryNode.parent = queryNode;
                existsQueryNode.linkField = (RefModel) field;
                existsQueryNode.join = Join.of(field.getName());
                existsQueryNode.addJoins(joins);

                String sql = existsQueryNode.getExistsSql(context);
                return new ConvertExistsSqlResult(sql, existsQueryNode.getFieldToSqlConverter(true, context));
            }
        };
    }

    /**
     * 字段到列转换，需要增加表别名前缀
     */
    private String convertFieldToColumn(String fieldPath, boolean haveRighted) {
        Tuple3<QueryNode, Field, BasicField> tuple3 = findField(fieldPath, haveRighted);

        QueryNode queryNode = tuple3.v1;
        String tableAs = queryNode.getTableAsName();
        Field field = tuple3.v2;
        BasicField subField = tuple3.v3;

        String columnName;
        if (subField != null) {
            columnName = subField.getColumnName();
        } else {
            if (!(field instanceof BasicField)) {
                throw new FieldParameterException("模型[" + modelContext.getModel().getName() + "]的字段[" + fieldPath + "]不是基本类型字段");
            }
            columnName = ((BasicField) field).getColumnName();
        }
        return tableAs + "." + columnName;
    }

    private ModelContext getRefModelContext(RefModel field) {
        ModelContext refModelContext = this.modelContext.getDataManagerGetter().getModelContext(field.getTargetModel());
        if (refModelContext == null) {
            throw new JoinException("模型[" + modelContext.getModel().getName() + "]的字段[" + field.getName() + "]的关联模型[" + field.getTargetModel() + "]没有被注册，请检查配置是否正确");
        }
        return refModelContext;
    }

    public String getTableAsName() {
        return DEFAULT_TABLE_AS_PREFIX + this.tableAsIndex;
    }

}
