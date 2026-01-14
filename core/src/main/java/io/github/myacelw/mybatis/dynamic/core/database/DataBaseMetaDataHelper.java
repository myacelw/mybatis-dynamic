package io.github.myacelw.mybatis.dynamic.core.database;

import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Index;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;

import java.util.List;

/**
 * 数据库元数据帮助类
 *
 * @author liuwei
 */
public interface DataBaseMetaDataHelper {

    String getDatabaseProductName();

    String unwrapIdentifier(String identifier);

    String getWrappedIdentifierInMeta(String identifier);

    Table getTable(String tableName, String schema);

    List<Column> getColumns(String tableName, String schema);

    List<Index> getIndexList(String tableName, String schema);
}
