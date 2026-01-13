package io.github.myacelw.mybatis.dynamic.core.database;

import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Index;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import lombok.SneakyThrows;

import java.util.List;

/**
 * 数据库元数据帮助类
 *
 * @author liuwei
 */
public interface DataBaseMetaDataHelper {
    @SneakyThrows
    Table getTable(String tableName, String schema);

    @SneakyThrows
    String getDatabaseProductName();

    @SneakyThrows
    String getIdentifierQuoteString();

    @SneakyThrows
    List<Column> getColumns(String tableName, String schema);

    @SneakyThrows
    List<Index> getIndexList(String tableName, String schema);
}
