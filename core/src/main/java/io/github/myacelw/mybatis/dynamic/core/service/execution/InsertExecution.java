package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.event.data.InsertDataEvent;
import io.github.myacelw.mybatis.dynamic.core.exception.data.DataException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.KeyGeneratorMode;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.GroupField;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.InsertCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import io.github.myacelw.mybatis.dynamic.core.util.DataUtil;
import io.github.myacelw.mybatis.dynamic.core.util.sequence.Sequence;
import io.github.myacelw.mybatis.dynamic.core.util.tuple.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 插入数据执行器
 *
 * @author liuwei
 */
@Slf4j
public class InsertExecution<ID> extends AbstractExecution<ID, Object, InsertCommand> {
    public final static String SQL = "INSERT INTO ${table} (${columns}) VALUES (${values})";


    @Override
    public Class<? extends Command> getCommandClass() {
        return InsertCommand.class;
    }

    @Override
    public Object exec(InsertCommand command, DataManager<ID> dataManager) {
        ModelContext modelContext = dataManager.getModelContext();
        Model model = modelContext.getModel();
        Object data = command.getData();
        KeyGeneratorMode keyGeneratorMode = command.isDisableGenerateId() ? KeyGeneratorMode.NONE : model.getUsedKeyGeneratorModel(modelContext.getDialect());
        BasicField generateValueField = model.getFirstPrimaryKeyFieldObj();

        if (data == null) {
            throw new DataException("Insert data for model [" + model.getName() + "] cannot be null");
        }
        //log.debug("insert data, table: {}, data: {}", model.getSchemaAndTableName(), data);

        Object genValue = null;
        List<String> skipFields = new ArrayList<>();
        List<FieldValue> fieldValues = new ArrayList<>();

        // 处理主键生成
        if (keyGeneratorMode == KeyGeneratorMode.SNOWFLAKE) {
            genValue = getSnowFlakeId(model);
            fieldValues.add(new FieldValue(generateValueField, genValue));
            skipFields.add(generateValueField.getName());
        } else if (keyGeneratorMode == KeyGeneratorMode.AUTO_INCREMENT || keyGeneratorMode == KeyGeneratorMode.SEQUENCE) {
            skipFields.add(generateValueField.getName());
        }

        convertTableDataForInsert(dataManager, data, fieldValues, skipFields, true);
        modelContext.getInterceptor().beforeInsert((DataManager<Object>) dataManager, data);

        // 执行 insert 返回产生的值
        Object insertGenValue = insert(modelContext, keyGeneratorMode, fieldValues);

        // 处理主键生成
        if (keyGeneratorMode == KeyGeneratorMode.AUTO_INCREMENT || keyGeneratorMode == KeyGeneratorMode.SEQUENCE) {
            genValue = convertGenValue(generateValueField.getJavaClass(), insertGenValue);
        }

        //得到主键值
        Object id = getId(keyGeneratorMode, model, data, genValue, generateValueField);

        modelContext.getInterceptor().afterInsert((DataManager<Object>) dataManager, data, id);
        modelContext.sendEvent(new InsertDataEvent(modelContext.getModel(), data));
        return id;
    }

    public static Object getSnowFlakeId(Model model) {
        Object result = null;
        long genValue = Sequence.getInstance().nextId();
        if (model.getFirstPrimaryKeyFieldObj().getJavaClass() == String.class) {
            result = String.valueOf(genValue);
        } else if (model.getFirstPrimaryKeyFieldObj().getJavaClass() == BigInteger.class) {
            result = BigInteger.valueOf(genValue);
        } else {
            result = genValue;
        }
        return result;
    }

    public static Object convertGenValue(Class<?> clazz, Object value) {
        if (clazz == String.class && !(value instanceof String)) {
            return String.valueOf(value);
        } else if (clazz == Integer.class && !(value instanceof Integer)) {
            return Integer.valueOf(String.valueOf(value));
        } else if (clazz == Long.class && !(value instanceof Long)) {
            return Long.valueOf(String.valueOf(value));
        } else if (clazz == BigInteger.class && !(value instanceof BigInteger)) {
            return new BigInteger(String.valueOf(value));
        }
        return value;
    }


    public static Object getId(KeyGeneratorMode keyGeneratorMode, Model model, Object data, Object genValue, BasicField generateValueField) {
        Object id = null;
        if (keyGeneratorMode == KeyGeneratorMode.NONE) {
            if (model.getPrimaryKeyFields().length == 1) {
                id = DataUtil.getProperty(data, model.getPrimaryKeyFields()[0]);
            } else if (model.getPrimaryKeyFields().length > 1) {
                Object[] multiIds = new Object[model.getPrimaryKeyFields().length];
                for (int i = 0; i < model.getPrimaryKeyFields().length; i++) {
                    String fieldName = model.getPrimaryKeyFields()[i];
                    Object value = DataUtil.getProperty(data, fieldName);
                    multiIds[i] = value;
                }
                id = multiIds;
            }
        } else if (keyGeneratorMode == KeyGeneratorMode.SNOWFLAKE || keyGeneratorMode == KeyGeneratorMode.AUTO_INCREMENT || keyGeneratorMode == KeyGeneratorMode.SEQUENCE) {
            if (model.getPrimaryKeyFields().length == 1) {
                id = genValue;
            } else if (model.getPrimaryKeyFields().length > 1) {
                Object[] multiIds = new Object[model.getPrimaryKeyFields().length];
                multiIds[0] = genValue;
                for (int i = 1; i < model.getPrimaryKeyFields().length; i++) {
                    String fieldName = model.getPrimaryKeyFields()[i];
                    Object value = DataUtil.getProperty(data, fieldName);
                    multiIds[i] = value;
                }
                id = multiIds;
            }
            DataUtil.setPropertyIgnoreError(data, generateValueField.getName(), genValue);
        }
        return id;
    }

    private static Object insert(ModelContext modelContext, KeyGeneratorMode keyGeneratorMode, List<FieldValue> fieldValues) {
        Map<String, Object> context = getSqlContext(modelContext, fieldValues);
        modelContext.getMybatisHelper().insert(modelContext.getSqlSession(), SQL, context, keyGeneratorMode, "genValue", modelContext.getModel().getTableDefine().getKeyGeneratorSequenceName());
        return context.get("genValue");
    }

    public static Map<String, Object> getSqlContext(ModelContext modelContext, List<FieldValue> fieldValues) {
        Map<String, Object> context = new HashMap<>();
        context.put("data", fieldValues);
        context.put("table", modelContext.getModel().getSchemaAndTableName());
        context.put("columns", fieldValues.stream().map(t -> t.getField().getColumnName()).collect(Collectors.joining(",")));
        List<String> params = new ArrayList<>(fieldValues.size());
        for (int j = 0; j < fieldValues.size(); j++) {
            FieldValue fieldValue = fieldValues.get(j);
            String typeHandler = fieldValue.getField().getTypeHandlerClass() != null ? ",typeHandler=" + fieldValue.getField().getTypeHandlerClass().getName() : "";
            String param = "#{data[" + j + "].value" + typeHandler + "}";
            params.add(param);
        }
        context.put("values", String.join(",", params));
        return context;
    }

    /**
     * 为Insert数据，字段数据列表转换为列
     */
    public static void convertTableDataForInsert(DataManager<?> dataManager, Object data, List<FieldValue> result, Collection<String> skipFields, boolean ignoreNull) {
        ModelContext modelContext = dataManager.getModelContext();

        BasicField deleteFlagField = modelContext.getDeleteFlagField();
        if (deleteFlagField != null) {
            result.add(new FieldValue(deleteFlagField, false));
        }

        convertTableDataForNoCommonField(modelContext, result, data, skipFields, ignoreNull);
        fill(dataManager, modelContext.getModel().getFields(), data, result);
    }

    private static void fill(DataManager<?> dataManager, List<? extends Field> allFields, Object data, List<FieldValue> fieldValues) {
        ModelContext modelContext = dataManager.getModelContext();
        allFields.stream()
                .map(field -> new Tuple<>(getFiller(modelContext, field, modelContext.getFillers()), field))
                .filter(tuple -> tuple.v1 != null)
                .sorted(Comparator.comparing(t -> t.v1.getOrder()))
                .forEach(tuple -> tuple.v1.insertFill(dataManager, (BasicField) tuple.v2, data, fieldValues));
    }

    public static void convertTableDataForNoCommonField(ModelContext modelContext, List<FieldValue> result, Object data, Collection<String> skipFields, boolean ignoreNull) {
        Map<String, Object> extProperties = modelContext.getModel().getExtProperties();
        log.debug("convertTableDataForNoCommonField: data map: {}", data);

        for (Field field : modelContext.getPermissionFields()) {
            log.debug("Processing field: {}", field.getName());
            if (skipFields != null && skipFields.contains(field.getName())) {
                log.debug("Skipping field: {}", field.getName());
                continue;
            }
            if ((field instanceof GroupField || field instanceof BasicField) && DataUtil.containsKey(data, field.getName())) {
                Object value = DataUtil.getProperty(data, field.getName());
                log.debug("Field {} found in data, value: {}", field.getName(), value);
                if (field instanceof GroupField) {
                    convertFieldGroupTableData(result, (GroupField) field, value, ignoreNull);
                } else {
                    if (value != null || !ignoreNull) {
                        result.add(new FieldValue((BasicField) field, value));
                    }
                }
            } else if (extProperties != null && extProperties.get(Model.EXT_PROPERTY_SUB_TYPE_MAP) != null &&
                    Objects.equals(extProperties.get(Model.EXT_PROPERTY_SUB_TYPE_FIELD_NAME), field.getName()) &&
                    !DataUtil.isBasicType(data.getClass())) {
                Map<String, Class<?>> subTypeMap = (Map<String, Class<?>>) extProperties.get(Model.EXT_PROPERTY_SUB_TYPE_MAP);
                subTypeMap.entrySet().stream()
                        .filter(t -> t.getValue().equals(data.getClass())).findFirst()
                        .ifPresent(t -> result.add(new FieldValue((BasicField) field, t.getKey())));
            } else {
                log.debug("Field {} not found in data map or not a basic/group field or not sub-type related.", field.getName());
            }
        }
    }

    public static void convertFieldGroupTableData(List<FieldValue> result, GroupField fieldGroup, Object value, boolean ignoreNull) {
        if (value == null) {
            if (!ignoreNull) {
                //FieldGroup作为一个整体进行更新, 如果为null则全部子字段全部更新为null。
                fieldGroup.getFields().forEach(subField ->
                        result.add(new FieldValue(subField, null))
                );
            }
        } else {
            fieldGroup.getFields().forEach(subField -> {
                if (DataUtil.containsKey(value, subField.getName())) {
                    Object value1 = DataUtil.getProperty(value, subField.getName());
                    if (value1 != null || !ignoreNull) {
                        result.add(new FieldValue(subField, value1));
                    }
                }
            });
        }
    }

}
