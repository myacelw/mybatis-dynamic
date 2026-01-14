package io.github.myacelw.mybatis.dynamic.core.service;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import lombok.NonNull;

import java.util.List;

/**
 * 模型到表的转换器
 *
 * @author liuwei
 */
public interface ModelToTableConverter {

    Table convertToTable(@NonNull Model model, List<String> fieldWhiteList);

    String getTableName(@NonNull String modelName, String customTableName);

    String getColumnName(@NonNull String fieldName, String customColumnName);

    String getIndexName(@NonNull String tableName, @NonNull String columnName, String customIndexName);

    String getWrappedIdentifierInMeta(String columnName);

    String getSchemaName(String schema);

    Column convertToColumn(Model model, BasicField field);
}
