package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.MysqlDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.OceanBaseDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.PostgresqlDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.typehandler.VectorTypeHandler;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.CustomSelectField;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.CustomCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.command.QueryCommand;
import io.github.myacelw.mybatis.dynamic.core.service.command.VectorRetrievalCommand;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelContext;

import java.util.Collections;
import java.util.List;

/**
 * 向量检索执行器
 *
 * @author liuwei
 */
public class VectorRetrievalExecution<ID, T> extends AbstractExecution<ID, List<T>, VectorRetrievalCommand<ID, T>> {
    private QueryExecution<ID, T> execution = new QueryExecution<>();

    @Override
    public Class<? extends Command> getCommandClass() {
        return VectorRetrievalCommand.class;
    }

    @Override
    public List<T> exec(VectorRetrievalCommand<ID, T> command, DataManager<ID> dataManager) {
        ModelContext modelContext = dataManager.getModelContext();

        OrderItem orderItem = new OrderItem();
        orderItem.setField(command.getEmbeddingField());
        orderItem.setFunctionTemplate(getFunctionTemplate(modelContext.getDialect(), true));
        orderItem.setFunctionValue(command.getQueryVector());

        QueryCommand<T> queryCommand = new QueryCommand<>();
        queryCommand.setClazz(command.getClazz());
        queryCommand.setOrderItems(Collections.singletonList(orderItem));
        queryCommand.setPage(new Page(1, command.getTopN()));

        if (command.getMaxDistance() != null) {
            CustomCondition c = CustomCondition.of(getFunctionTemplate(modelContext.getDialect(), false) + " <= " + command.getMaxDistance(), command.getEmbeddingField(), command.getQueryVector());
            queryCommand.setCondition(GroupCondition.and(command.getCondition(), c));
        } else {
            queryCommand.setCondition(command.getCondition());
        }

        queryCommand.setSelectFields(command.getSelectFields());

        if (command.getSelectDistanceFieldName() != null) {
            CustomSelectField customSelectField = new CustomSelectField();
            customSelectField.setFields(Collections.singletonList(command.getEmbeddingField()));
            customSelectField.setSqlTemplate(getFunctionTemplate(modelContext.getDialect(), false));
            customSelectField.setValue(command.getQueryVector());
            customSelectField.setJavaType(Double.class);
            customSelectField.setName(command.getSelectDistanceFieldName());
            queryCommand.setCustomSelectFields(Collections.singletonList(customSelectField));
        }

        return execution.exec(queryCommand, dataManager);
    }

    protected String getFunctionTemplate(DataBaseDialect dataBaseDialect, boolean approximate) {
        if (dataBaseDialect instanceof OceanBaseDataBaseDialect) {
            return "l2_distance($COL, #{EXPR,typeHandler=" + VectorTypeHandler.class.getName() + "})" + (approximate ? " APPROXIMATE" : "");
        } else if (dataBaseDialect instanceof MysqlDataBaseDialect) {
            //注意：mysql9企业版才支持DISTANCE函数
            return "DISTANCE($COL, #{EXPR,typeHandler=" + VectorTypeHandler.class.getName() + "}, 'COSINE')";
        } else if (dataBaseDialect instanceof PostgresqlDataBaseDialect)
            return "$COL <-> #{EXPR,typeHandler=" + VectorTypeHandler.class.getName() + "}";
        else {
            throw new UnsupportedOperationException("不支持的数据库类型：" + dataBaseDialect.getName());
        }
    }

}
