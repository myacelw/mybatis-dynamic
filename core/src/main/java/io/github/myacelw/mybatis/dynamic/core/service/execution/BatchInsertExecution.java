package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.event.data.BatchInsertDataEvent;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.KeyGeneratorMode;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.FieldValue;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.BatchInsertCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 批量插入执行器
 *
 * @author liuwei
 */
@Slf4j
public class BatchInsertExecution<ID> extends AbstractExecution<ID, List<ID>, BatchInsertCommand> {

    @Override
    public Class<? extends Command> getCommandClass() {
        return BatchInsertCommand.class;
    }

    @Override
    public List<ID> exec(BatchInsertCommand command, DataManager<ID> dataManager) {
        ModelContext modelContext = dataManager.getModelContext();
        Model model = modelContext.getModel();
        KeyGeneratorMode keyGeneratorMode = command.isDisableGenerateId() ? KeyGeneratorMode.NONE : model.getUsedKeyGeneratorModel(modelContext.getDialect());
        BasicField generateValueField = model.getFirstPrimaryKeyFieldObj();

        List<?> dataList = command.getData();
        if (dataList == null || dataList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Object> genValueList = null;
        List<String> skipFields = new ArrayList<>();
        List<List<FieldValue>> fieldValuesList = new ArrayList<>();

        for (Object o : dataList) {
            List<FieldValue> fieldValues = new ArrayList<>();
            fieldValuesList.add(fieldValues);
        }

        // 处理主键生成
        if (keyGeneratorMode == KeyGeneratorMode.SNOWFLAKE) {
            genValueList = new ArrayList<>();
            for (List<FieldValue> fieldValues : fieldValuesList) {
                Object genValue = InsertExecution.getSnowFlakeId(model);
                fieldValues.add(new FieldValue(generateValueField, genValue));
                genValueList.add(genValue);
            }
            skipFields.add(generateValueField.getName());
        } else if (keyGeneratorMode == KeyGeneratorMode.AUTO_INCREMENT || keyGeneratorMode == KeyGeneratorMode.SEQUENCE) {
            skipFields.add(generateValueField.getName());
        }

        Iterator<?> dataIterator = dataList.iterator();
        for (List<FieldValue> fieldValues : fieldValuesList) {
            InsertExecution.convertTableDataForInsert(dataManager, dataIterator.next(), fieldValues, skipFields, false);
        }

        modelContext.getInterceptor().beforeBatchInsert((DataManager<Object>) dataManager, dataList);

        // 执行 insert 返回产生的值
        List<Object> insertGenValueList = batchInsert(modelContext, keyGeneratorMode, fieldValuesList);


        // 处理主键生成
        if (keyGeneratorMode == KeyGeneratorMode.AUTO_INCREMENT || keyGeneratorMode == KeyGeneratorMode.SEQUENCE) {
            if (!ObjectUtil.isEmpty(insertGenValueList)) {
                genValueList = insertGenValueList.stream().map(t -> InsertExecution.convertGenValue(generateValueField.getJavaClass(), t)).collect(Collectors.toList());
            }
        }

        List<ID> idList = new ArrayList<>();

        Iterator<?> genValueIterator = genValueList == null ? null : genValueList.iterator();
        for (Object data : dataList) {
            Object genValue = genValueIterator == null ? null : genValueIterator.next();
            Object id = InsertExecution.getId(keyGeneratorMode, model, data, genValue, generateValueField);
            idList.add((ID) id);
        }
        //得到主键值

        modelContext.getInterceptor().afterBatchInsert((DataManager<Object>) dataManager, dataList, idList);
        modelContext.sendEvent(new BatchInsertDataEvent(modelContext.getModel(), dataList));

        return idList;
    }

    private static List<Object> batchInsert(ModelContext modelContext, KeyGeneratorMode keyGeneratorMode, List<List<FieldValue>> fieldValuesList) {
        List<Object> contexts = new ArrayList<>();
        for (List<FieldValue> fieldValues : fieldValuesList) {
            contexts.add(InsertExecution.getSqlContext(modelContext, fieldValues));
        }

        modelContext.getMybatisHelper().batchInsert(InsertExecution.SQL, contexts, 2000, keyGeneratorMode, "genValue", modelContext.getModel().getTableDefine().getKeyGeneratorSequenceName());
        if (keyGeneratorMode == KeyGeneratorMode.AUTO_INCREMENT || keyGeneratorMode == KeyGeneratorMode.SEQUENCE) {
            List<Object> genValueList = new ArrayList<>();
            for (Object context : contexts) {
                Map<String, Object> map = (Map<String, Object>) context;
                genValueList.add(map.get("genValue"));
            }
            return genValueList;
        }
        return null;
    }

}
