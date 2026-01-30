package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.exception.crud.FieldParameterException;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.filler.Filler;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 抽象的命令执行器
 *
 * @param <R>
 */
@Setter
@Slf4j
public abstract class AbstractExecution<ID, R, C extends Command> implements Execution<ID, R, C> {

    protected static String convertColumnForAllField(ModelContext context, String fieldName) {
        Field field = context.getFieldMap().get(fieldName);
        if (field == null) {
            throw new FieldParameterException("Field [" + fieldName + "] not found in model [" + context.getModel().getName() + "]");
        }
        if (!(field instanceof BasicField)) {
            throw new FieldParameterException("Field [" + fieldName + "] in model [" + context.getModel().getName() + "] is not a basic type and cannot be used as a condition");
        }
        return ((BasicField) field).getColumnName();
    }

    protected static String[] convertColumnForAllField(ModelContext context, String[] fieldNames) {
        String[] columns = new String[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            columns[i] = convertColumnForAllField(context, fieldNames[i]);
        }
        return columns;
    }

    protected static Filler getFiller(ModelContext context, Field field, Map<String, Filler> fillers) {
        if (!(field instanceof BasicField)) {
            return null;
        }
        BasicField f = (BasicField) field;
        if (f.getFillerName() == null || f.getFillerName().isEmpty()) {
            return null;
        }
        Filler filler = fillers.get(f.getFillerName());
        if (filler == null) {
            throw new FieldParameterException("Filler [" + f.getFillerName() + "] for field [" + f.getName() + "] in model [" + context.getModel().getName() + "] not found");
        }
        return filler;
    }

}
