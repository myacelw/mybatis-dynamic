package io.github.myacelw.mybatis.dynamic.core.database.dialect;

import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.Sql;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 数据库方言接口
 *
 * @author liuwei
 */
public interface DataBaseDialect {

    int PRIORITY_DEFAULT = 10;

    /**
     * 数据库名称
     *
     * @return the name of the database
     */
    String getName();

    /**
     * 是否支持自动增长
     *
     * @return true if the database supports auto increment.
     */
    boolean supportAutoIncrement();

    /**
     * 是否支持序列
     *
     * @return true if the database supports sequence.
     */
    boolean supportSequence();

    /**
     * 是否支持不同表间索引名相同
     */
    boolean supportSameIndexNameInTable();

    List<Sql> getCreateTableSql(Table table);

    Sql getDropTableSql(Table table);

    Sql getRenameTableSql(Table oldTable, Table newTable);

    List<Sql> getAddColumnSql(Table table, Column column);

    Sql getDropColumnSql(Table table, Column column);

    List<Sql> getAlterColumnTypeSql(Table table, Column column, Column oldColumn);

    Sql getSetTableCommentSql(Table table);

    /**
     * 变更列同时变更注释
     */
    default boolean isAlertColumnIncludeComment() {
        return false;
    }

    Sql getSetColumnCommentSql(Table table, Column column);

    Sql getAddIndexSql(Table table, Column column, String indexName);

    Sql getDropIndexSql(Table table, String indexName);

    String getListAggFunctionSql(String columnName);

    String getListAggDistinctFunctionSql(String columnName);

    String getJsonArrayAggFunctionSql(String columnName);

    String getJsonArrayAggDistinctFunctionSql(String columnName);

    /**
     * 规范化列类型
     */
    void normalizeColumn(Column column);

    /**
     * 优先级
     *
     * @return the priority of this DataBase implementation. Lower numbers are first.
     */
    default int getPriority() {
        return PRIORITY_DEFAULT;
    }

    static List<DataBaseDialect> getInstances() {
        ServiceLoader<DataBaseDialect> loader = ServiceLoader.load(DataBaseDialect.class);
        List<DataBaseDialect> dataBaseList = new ArrayList<>();
        for (DataBaseDialect db : loader) {
            dataBaseList.add(db);
        }
        dataBaseList.sort(Comparator.comparingInt(DataBaseDialect::getPriority));
        return dataBaseList;
    }
}
