package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.exception.crud.FieldParameterException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.ToManyField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.ToOneField;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.FillDataCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.IdUtil;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.DataUtil;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 填充数据执行器
 *
 * @author liuwei
 */
public class FillDataExecution<ID> extends AbstractExecution<ID, Void, FillDataCommand> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return FillDataCommand.class;
    }

    @Override
    public Void exec(FillDataCommand command, DataManager<ID> dataManager) {
        ModelContext modelContext = dataManager.getModelContext();
        if (ObjectUtil.isEmpty(command.getData())) {
            return null;
        }

        if (command.getFillFields() != null) {
            for (FillDataCommand.FillField fill : command.getFillFields()) {
                Field field = modelContext.getPermissionedField(fill.getFieldName(), true);
                if (field instanceof ToOneField) {
                    fillManyToOneData(dataManager, command, (ToOneField) field, fill);
                } else if (field instanceof ToManyField) {
                    fillOneToManyData(dataManager, command, (ToManyField) field, fill);
                } else {
                    throw new FieldParameterException("模型[" + modelContext.getModel().getName() + "]的字段[" + fill.getFieldName() + "]不是关联类型或者关联模型类型");
                }
            }
        }

        return null;
    }

    private void fillOneToManyData(DataManager<ID> dataManager, FillDataCommand command, ToManyField field, FillDataCommand.FillField fill) {
        ModelContext modelContext = dataManager.getModelContext();
        DataManager<Object> targetDataManager = modelContext.getDataManagerGetter().getDataManager(field.getTargetModel(), dataManager.getModelContext().getSqlSession());

        if (targetDataManager == null) {
            throw new FieldParameterException("模型[" + modelContext.getModel().getName() + "]的填充字段模型[" + field.getTargetModel() + "]没有找到");
        }

        String[] joinTargetFields = field.getJoinTargetFields();
        Assert.isTrue(joinTargetFields.length == dataManager.getModel().getPrimaryKeyFields().length, "关联模型字段数量必须与当前模型主键数量一致, 模型：" + dataManager.getModel().getName() + ",字段：" + field.getName());

        List<Object> ids = getIdList(command.getData(), dataManager.getModel().getPrimaryKeyFields());
        if (ObjectUtil.isEmpty(ids)) {
            return;
        }

        List<?> relValueList = targetDataManager.queryChain(field.getJavaClass())
                .where(IdUtil.getIdsCondition(joinTargetFields, ids))
                .select(fill.getSelectFields())
                .joins(fill.getJoins())
                .exec();

        Map<Object, List<Object>> extValueMap = new HashMap<>();
        for (Object item : relValueList) {
            Object fkId = DataUtil.getSingleOrMultiValue(item, field.getJoinTargetFields());
            extValueMap.computeIfAbsent(fkId, k -> new ArrayList<>()).add(item);
        }

        setFieldValues(dataManager, command, fill.getFieldName(), extValueMap, ArrayList::new);
    }

    private void fillManyToOneData(DataManager<ID> dataManager, FillDataCommand command, ToOneField field, FillDataCommand.FillField fill) {
        ModelContext modelContext = dataManager.getModelContext();
        DataManager<?> targetDataManager = modelContext.getDataManagerGetter().getDataManager(field.getTargetModel(), dataManager.getModelContext().getSqlSession());

        if (targetDataManager == null) {
            throw new FieldParameterException("模型[" + modelContext.getModel().getName() + "]的填充字段模型[" + field.getTargetModel() + "]没有找到");
        }
        String[] joinLocalFields = field.getJoinLocalFields();

        Assert.isTrue(joinLocalFields.length == targetDataManager.getModel().getPrimaryKeyFields().length, "关联模型字段数量必须与目标模型主键数量一致, 模型：" + dataManager.getModel().getName() + ",字段：" + field.getName());

        Map<Object, List<Object>> fkIdToDataMap = new HashMap<>();

        for (Object item : command.getData()) {
            Object fkId = DataUtil.getSingleOrMultiValue(item, joinLocalFields);
            if (!ObjectUtil.isEmpty(fkId)) {
                fkIdToDataMap.computeIfAbsent(fkId, k -> new ArrayList<>()).add(item);
            }
        }

        if (ObjectUtil.isEmpty(fkIdToDataMap)) {
            return;
        }

        List<?> relValueList = targetDataManager.queryChain(field.getJavaClass())
                .where(IdUtil.getIdsCondition(targetDataManager.getModel().getPrimaryKeyFields(), fkIdToDataMap.keySet()))
                .select(fill.getSelectFields())
                .joins(fill.getJoins())
                .exec();
        setToOneValues(targetDataManager.getModel(), fkIdToDataMap, relValueList, field.getName());
    }

    private void setFieldValues(DataManager<ID> dataManager, FillDataCommand command, String fieldName, Map<Object, ?> valueMap, Supplier<?> defaultValue) {
        if (ObjectUtil.isEmpty(valueMap)) {
            return;
        }
        String[] primaryKeyFieldNames = dataManager.getModel().getPrimaryKeyFields();
        for (Object data : command.getData()) {
            Object id = DataUtil.getSingleOrMultiValue(data, primaryKeyFieldNames);
            Object value = valueMap.get(id);
            if (value != null) {
                DataUtil.setProperty(data, fieldName, value);
            } else {
                DataUtil.setProperty(data, fieldName, defaultValue.get());
            }
        }
    }

    public void setToOneValues(Model targetModel, Map<Object, List<Object>> fkIdToDataMap, List<?> relValueList, String fieldName) {
        if (ObjectUtil.isEmpty(relValueList)) {
            return;
        }
        for (Object v : relValueList) {
            Object id = DataUtil.getSingleOrMultiValue(v, targetModel.getPrimaryKeyFields());
            for (Object data : fkIdToDataMap.get(id)) {
                DataUtil.setProperty(data, fieldName, v);
            }
        }
    }

    private static List<Object> getIdList(List<?> result, String[] primaryKeyFieldNames) {
        if (result == null) {
            return Collections.emptyList();
        }
        return result.stream().filter(Objects::nonNull).map(t -> DataUtil.getSingleOrMultiValue(t, primaryKeyFieldNames)).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }


}
