package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.FillDataCommand;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 填充数据处理链
 *
 * @author liuwei
 */
public class FillDataChain<ID> extends AbstractChain<ID, Void, FillDataCommand, FillDataChain<ID>> {

    public FillDataChain(DataManager<ID> dataManager) {
        super(dataManager, FillDataCommand::new);
    }

    /**
     * 设置需要填充的数据列表
     */
    public FillDataChain<ID> data(List<?> data) {
        command.setData(data);
        return this;
    }

    /**
     * 设置需要填充的单条数据
     */
    public FillDataChain<ID> singleData(Object data) {
        add(command::getData, command::setData, data);
        return this;
    }

    public FillDataChain<ID> fillFields(List<FillDataCommand.FillField> fillFields) {
        add(command::getFillFields, command::setFillFields, fillFields);
        return this;
    }

    public FillDataChain<ID> fillField(FillDataCommand.FillField fillField) {
        add(command::getFillFields, command::setFillFields, fillField);
        return this;
    }

    public FillDataChain<ID> fillField(@NonNull String fieldName, String... selectFields) {
        FillDataCommand.FillField field = new FillDataCommand.FillField(fieldName);
        if (!ObjectUtil.isEmpty(selectFields)) {
            field.setSelectFields(Arrays.asList(selectFields));
        }
        add(command::getFillFields, command::setFillFields, field);
        return this;
    }

    public FillDataChain<ID> fillField(@NonNull String fieldName, Consumer<FillFieldBuilder> builderConsumer) {
        FillFieldBuilder builder = new FillFieldBuilder(fieldName);
        if (builderConsumer != null) {
            builderConsumer.accept(builder);
        }
        FillDataCommand.FillField field = builder.build();
        add(command::getFillFields, command::setFillFields, field);
        return this;
    }


    public static class FillFieldBuilder {

        FillDataCommand.FillField fillField = new FillDataCommand.FillField();

        FillFieldBuilder(String fieldName) {
            fillField.setFieldName(fieldName);
        }

        public FillDataCommand.FillField build() {
            return fillField;
        }

        public FillFieldBuilder joins(List<Join> joins) {
            add(fillField::getJoins, fillField::setJoins, joins);
            return this;
        }

        public FillFieldBuilder joins(Join joins) {
            add(fillField::getJoins, fillField::setJoins, joins);
            return this;
        }

        public FillFieldBuilder join(Join join) {
            add(fillField::getJoins, fillField::setJoins, join);
            return this;
        }

        public FillFieldBuilder selectFields(List<String> selectFields) {
            fillField.setSelectFields(selectFields);
            return this;
        }

        public FillFieldBuilder selectField(String selectField) {
            add(fillField::getSelectFields, fillField::setSelectFields, selectField);
            return this;
        }
    }


}
