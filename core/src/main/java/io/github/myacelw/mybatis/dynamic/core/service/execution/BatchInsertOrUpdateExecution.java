package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.event.data.BatchInsertDataEvent;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.BatchInsertOrUpdateCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 批量插入或更新执行器
 * @author liuwei
 */
public class BatchInsertOrUpdateExecution<ID> extends AbstractExecution<ID, Void, BatchInsertOrUpdateCommand> {

    public static final String SQL = "INSERT INTO ${table} (${columns}) VALUES (${values}) ON DUPLICATE KEY UPDATE ${set}";

    @Override
    public Class<? extends Command> getCommandClass() {
        return BatchInsertOrUpdateCommand.class;
    }

    @Override
    public Void exec(BatchInsertOrUpdateCommand command, DataManager<ID> dataManager) {
        ModelContext modelContext = dataManager.getModelContext();
        List<?> dataList = command.getData();
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }
        Set<String> updateIgnoreColumns = getUpdateIgnoreColumns(command, modelContext);

        Field field =  dataManager.getModelContext().getField(Model.FIELD_DELETE_FLAG);
        if (field instanceof BasicField) {
            updateIgnoreColumns.add(((BasicField) field).getColumnName());
        }

        List<List<FieldValue>> newDataList = new ArrayList<>();
        for (Object data : dataList) {
            List<FieldValue> fieldValues = new ArrayList<>();
            InsertExecution.convertTableDataForInsert(dataManager, data, fieldValues, Collections.emptyList(), false);
            newDataList.add(fieldValues);
        }

        modelContext.getInterceptor().beforeBatchInsertOrUpdate((DataManager) dataManager, dataList);
        batchInsertOrUpdate(modelContext, newDataList, command.getBatchSize());
        modelContext.getInterceptor().beforeBatchInsertOrUpdate((DataManager) dataManager, dataList);
        modelContext.sendEvent(new BatchInsertDataEvent(dataManager.getModel(), dataList));
        return null;
    }

    private Set<String> getUpdateIgnoreColumns(BatchInsertOrUpdateCommand command, ModelContext modelContext) {
        Set<String> updateIgnoreColumns = new HashSet<>();
        if (command.getUpdateIgnoreFields() != null) {
            for (String updateIgnoreField : command.getUpdateIgnoreFields()) {
                modelContext.getPermissionFields().forEach(v -> {
                    if (v.getName().equals(updateIgnoreField) && v instanceof BasicField) {
                        updateIgnoreColumns.add(((BasicField) v).getColumnName());
                    }
                });
            }
        }
        return updateIgnoreColumns;
    }

    private static void batchInsertOrUpdate(ModelContext modelContext, List<List<FieldValue>> fieldValuesList, int batchSize) {
        List<Object> contexts = fieldValuesList.stream().map(t -> getSqlContext(modelContext, t)).collect(Collectors.toList());
        modelContext.getMybatisHelper().batchUpdate(SQL, contexts, batchSize);
    }

    public static Map<String, Object> getSqlContext(ModelContext modelContext, List<FieldValue> fieldValues) {
        Map<String, Object> context = InsertExecution.getSqlContext(modelContext, fieldValues);

        int i = 0;
        List<String> setList = new ArrayList<>();
        for (FieldValue fieldValue : fieldValues) {
            if (!modelContext.getModel().isPrimaryKeyField(fieldValue.getField().getName())) {
                setList.add(fieldValue.getField().getColumnName() + " = #{data[" + i + "].value}");
            }
            i++;
        }
        context.put("set", String.join(", ", setList));
        return context;
    }


}
