package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.event.data.UpdateDataEvent;
import io.github.myacelw.mybatis.dynamic.core.exception.crud.ConditionParameterException;
import io.github.myacelw.mybatis.dynamic.core.exception.data.DataException;
import io.github.myacelw.mybatis.dynamic.core.exception.data.DataNotFoundException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.GroupField;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.UpdateCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.IdUtil;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.DataUtil;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import io.github.myacelw.mybatis.dynamic.core.util.tuple.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 更新数据执行器
 *
 * @author liuwei
 */
@Slf4j
public class UpdateExecution<ID> extends AbstractExecution<ID, Void, UpdateCommand<ID>> {
    public static final String SQL = "UPDATE ${table} SET ${set} WHERE ${where}";

    @Override
    public Class<? extends Command> getCommandClass() {
        return UpdateCommand.class;
    }

    @Override
    public Void exec(UpdateCommand<ID> command, DataManager<ID> dataManager) {
        ModelContext modelContext = dataManager.getModelContext();
        Model model = modelContext.getModel();

        Object data = command.getData();
        Object id = command.getId() != null ? command.getId() : IdUtil.getId(model, data, true);

        if (ObjectUtil.isEmpty(id)) {
            throw new DataException("更新模型[" + model.getName() + "]数据操作ID不能为空");
        }

        if (data instanceof Map && ((Map<?, ?>) data).isEmpty()) {
            log.info("更新数据为空，跳过执行更新操作，id:{}", id);
            return null;
        }

        List<FieldValue> changedList;
        if (!command.isForce()) {
            Map<String, Object> oldData = dataManager.getByIdChain().id((ID) id).exec();
            if (oldData == null) {
                throw new DataException("更新模型[" + model.getName() + "]数据操作ID[" + id + "]对应数据不存在");
            }
            changedList = getChangedFieldValues(modelContext, data, oldData, command);
        } else {
            changedList = getChangedFieldValues(modelContext, data, command);
        }

        fill(dataManager, data, changedList);

        if (changedList.isEmpty() && (command.getCustomSetList() == null || command.getCustomSetList().isEmpty())) {
            log.debug("更新数据为空，跳过执行更新操作，model:{}, id:{}", model.getName(), id);
            return null;
        }
        Condition additionalCondition = command.isIgnoreLogicDelete() ? modelContext.getAdditionalIgnoreDeleteCondition() : modelContext.getAdditionalCondition();
        Condition idCondition = IdUtil.getIdCondition(model, id);

        Condition condition = additionalCondition == null ? idCondition : GroupCondition.and(additionalCondition, idCondition);

        modelContext.getInterceptor().beforeUpdate((DataManager) dataManager, id, data, changedList);
        int n = update(modelContext, changedList, command.getCustomSetList(), condition);
        if (n == 0) {
            throw new DataNotFoundException("更新模型[" + model.getName() + "]数据操作ID[" + id + "]对应数据不存在");
        }

        modelContext.getInterceptor().afterUpdate((DataManager) dataManager, id, data, changedList);
        modelContext.sendEvent(new UpdateDataEvent<>(model, id, data));

        return null;
    }

    private static Map<String, String> getCustomSetSqlMap(List<UpdateCommand.CustomSet> customSetList, ModelContext modelContext) {
        if (customSetList == null || customSetList.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> customSqlMap = new HashMap<>();

        int i = 0;
        for (UpdateCommand.CustomSet customSet : customSetList) {
            Field field = modelContext.getPermissionedField(customSet.getUpdateField(), true);
            Assert.isTrue(field instanceof BasicField, "更新字段" + customSet.getUpdateField() + "不是基本字段，无法更新");

            String setColumn = ((BasicField) field).getColumnName();

            String sql = customSet.getSqlTemplate().replace("EXPR", "s[" + i + "].value");

            if (customSet.getFields() != null) {
                int j = 0;
                String firstColumnName = null;
                for (String f : customSet.getFields()) {
                    Field field2 = modelContext.getPermissionedField(f, true);
                    Assert.isTrue(field2 instanceof BasicField, "字段" + customSet.getUpdateField() + "不是基本字段，无法作为参数");

                    String columnName = ((BasicField) field2).getColumnName();
                    sql = sql.replace("$COL[" + j + "]", columnName);
                    if (j == 0) {
                        firstColumnName = columnName;
                    }
                    j++;
                }
                if (firstColumnName != null) {
                    sql = sql.replace("$COL", firstColumnName);
                }
            }

            customSqlMap.put(setColumn, sql);
            i++;
        }
        return customSqlMap;
    }

    public static int update(ModelContext modelContext, @NonNull List<FieldValue> fieldValues, Condition condition) {
        return update(modelContext, fieldValues, null, condition);
    }

    public static int update(ModelContext modelContext, @NonNull List<FieldValue> fieldValues, List<UpdateCommand.CustomSet> customSetList, Condition condition) {
        Map<String, Object> context = getSqlContext(modelContext, fieldValues, customSetList, condition);
        log.debug("UPDATE DATA, table: {}, context:{}", modelContext.getModel().getSchemaAndTableName(), context);
        return modelContext.getMybatisHelper().update(modelContext.getSqlSession(), SQL, context);
    }

    public static Map<String, Object> getSqlContext(ModelContext modelContext, List<FieldValue> fieldValues, List<UpdateCommand.CustomSet> customSetList, Condition condition) {
        String whereSql = condition.sql("c", t -> convertColumnForAllField(modelContext, t), modelContext.getDialect());
        if (!StringUtil.hasText(whereSql)) {
            throw new ConditionParameterException("更新模型[" + modelContext.getModel().getName() + "]数据操作查询条件不能为空");
        }

        int i = 0;
        List<String> setList = new ArrayList<>();

        Map<String, String> customSetSqlMap = getCustomSetSqlMap(customSetList, modelContext);

        customSetSqlMap.forEach((columnName, sql) -> setList.add(columnName + " = " + sql));

        for (FieldValue fieldValue : fieldValues) {
            if (!customSetSqlMap.containsKey(fieldValue.getField().getColumnName())) {
                setList.add(fieldValue.getField().getColumnName() + " = #{data[" + (i++) + "].value}");
            }
        }

        Map<String, Object> context = new HashMap<>();
        context.put("c", condition);
        context.put("s", customSetList);
        context.put("data", fieldValues);
        context.put("setList", customSetSqlMap);
        context.put("table", modelContext.getModel().getSchemaAndTableName());
        context.put("set", String.join(", ", setList));
        context.put("where", whereSql);
        return context;
    }

    public static void fill(DataManager<?> dataManager, Object data, List<FieldValue> result) {
        dataManager.getModel().getFields().stream()
                .map(field -> new Tuple<>(getFiller(dataManager.getModelContext(), field, dataManager.getModelContext().getFillers()), field))
                .filter(tuple -> tuple.v1 != null)
                .sorted(Comparator.comparing(t -> t.v1.getOrder()))
                .forEach(tuple -> tuple.v1.updateFill(dataManager, (BasicField) tuple.v2, data, result));
    }


    /**
     * 得到新老变化数据，只得到变化的可以转换为列非公共的字段数据。
     */
    private static List<FieldValue> getChangedFieldValues(ModelContext modelContext, Object newData, Object oldData, UpdateCommand<?> command) {
        //变更的数据
        List<FieldValue> changedData = new ArrayList<>();

        Collection<Field> fields = getUpdateFields(modelContext, command);

        for (Field field : fields) {
            if (!Model.FIELD_DELETE_FLAG.equals(field.getName())
                    && !modelContext.getModel().isPrimaryKeyField(field.getName())
                    && (field instanceof GroupField || field instanceof BasicField)
                    && DataUtil.containsKey(newData, field.getName())) {
                addChangedFieldValue(field, newData, oldData, command.isUpdateOnlyNonNull(), changedData);
            }
        }
        return changedData;
    }

    private static Collection<Field> getUpdateFields(ModelContext modelContext, UpdateCommand<?> command) {
        return modelContext.getPermissionFields().stream()
                .filter(t -> ObjectUtil.isEmpty(command.getUpdateFields()) || command.getUpdateFields().contains(t.getName()))
                .filter(t -> ObjectUtil.isEmpty(command.getIgnoreFields()) || !command.getIgnoreFields().contains(t.getName()))
                .collect(Collectors.toList());
    }

    private static void addChangedFieldValue(Field field, Object newData, Object oldData, boolean updateOnlyNonNull, List<FieldValue> changedData) {
        Object newValue = DataUtil.getProperty(newData, field.getName());
        if (updateOnlyNonNull && newValue == null) {
            return;
        }

        Object oldValue = DataUtil.getProperty(oldData, field.getName());
        if (field instanceof BasicField) {
            if (!Objects.equals(newValue, oldValue)) {
                changedData.add(new FieldValue((BasicField) field, newValue));
            }
        } else if (field instanceof GroupField) {
            GroupField groupField = (GroupField) field;
            if (Objects.equals(newValue, oldValue)) {
                return;
            }
            for (BasicField subField : groupField.getFields()) {
                Object subNewValue = newValue == null ? null : DataUtil.getProperty(newValue, subField.getName());
                Object subOldValue = oldValue == null ? null : DataUtil.getProperty(oldValue, subField.getName());
                if (!(updateOnlyNonNull && subNewValue == null) && !Objects.equals(subNewValue, subOldValue)) {
                    changedData.add(new FieldValue(subField, subNewValue));
                }
            }
        }
    }

    public static List<FieldValue> getChangedFieldValues(ModelContext modelContext, Object newData, boolean updateOnlyNonNull, boolean ignorePrimaryKey) {
        return getChangedFieldValues(modelContext, newData, modelContext.getPermissionFields(), updateOnlyNonNull, ignorePrimaryKey);
    }

    private static List<FieldValue> getChangedFieldValues(ModelContext modelContext, Object newData, UpdateCommand<?> command) {
        Collection<Field> fields = getUpdateFields(modelContext, command);
        return getChangedFieldValues(modelContext, newData, fields, command.isUpdateOnlyNonNull(), true);
    }

    private static List<FieldValue> getChangedFieldValues(ModelContext modelContext, Object newData, Collection<Field> fields, boolean updateOnlyNonNull, boolean ignorePrimaryKey) {
        //变更的数据
        List<FieldValue> changedData = new ArrayList<>();

        for (Field field : fields) {
            if (!Model.FIELD_DELETE_FLAG.equals(field.getName())
                    && (!ignorePrimaryKey || !modelContext.getModel().isPrimaryKeyField(field.getName()))
                    && (field instanceof GroupField || field instanceof BasicField)
                    && DataUtil.containsKey(newData, field.getName())) {
                addChangedFieldValue(field, newData, updateOnlyNonNull, changedData);
            }
        }
        return changedData;
    }

    private static void addChangedFieldValue(Field field, Object newData, boolean updateOnlyNonNull, List<FieldValue> changedData) {
        Object newValue = DataUtil.getProperty(newData, field.getName());

        if (updateOnlyNonNull && newValue == null) {
            return;
        }
        if (field instanceof BasicField) {
            changedData.add(new FieldValue((BasicField) field, newValue));
        } else if (field instanceof GroupField) {
            GroupField groupField = (GroupField) field;
            for (BasicField subField : groupField.getFields()) {
                Object subNewValue = newValue == null ? null : DataUtil.getProperty(newValue, subField.getName());
                if (!(updateOnlyNonNull && subNewValue == null)) {
                    changedData.add(new FieldValue(subField, subNewValue));
                }
            }
        }
    }

}
