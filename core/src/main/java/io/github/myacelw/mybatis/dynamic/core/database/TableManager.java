package io.github.myacelw.mybatis.dynamic.core.database;

import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;

/**
 * 表管理器，实现表创建、更新、改名和删除。
 *
 * @author liuwei
 */
public interface TableManager {

    DataBaseMetaDataHelper getMetaDataHelper();

    /**
     * 创建或更新表结构
     */
    void createOrUpgradeTable(Table table);

    /**
     * 删除表结构
     */
    void dropTable(Table table);

    /**
     * 修改表名
     */
    void rename(Table oldTable, Table newTable);

}
