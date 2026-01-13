package io.github.myacelw.mybatis.dynamic.core.database.impl;

import io.github.myacelw.mybatis.dynamic.core.database.DataBaseMetaDataHelper;
import io.github.myacelw.mybatis.dynamic.core.database.MybatisHelper;
import io.github.myacelw.mybatis.dynamic.core.database.TableManager;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.MysqlDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.AlterOrDropStrategy;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Index;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.Sql;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 数据库表管理器实现类。
 *
 * @author liuwei
 */
@Slf4j
public class TableManagerImpl implements TableManager {

    public static final String CONST_SQL_KEY = "__sql";
    public static final String CONST_SQL_EXP = "${" + CONST_SQL_KEY + "}";

    @Getter
    private final DataBaseMetaDataHelper metaDataHelper;

    @Getter
    private final MybatisHelper sqlHelper;

    @Getter
    private final DataBaseDialect dialect;

    public TableManagerImpl(@NonNull DataBaseMetaDataHelper metaDataHelper, @NonNull MybatisHelper mybatisHelper, DataBaseDialect dialect) {
        this.metaDataHelper = metaDataHelper;
        this.sqlHelper = mybatisHelper;
        this.dialect = dialect == null ? new MysqlDataBaseDialect() : dialect;
    }

    @Override
    public void createOrUpgradeTable(@NonNull Table table) {
        Table currentTable = getCurrentTableOrRename(table);
        modifyTableComment(table, currentTable);

        List<Column> columns = getCurrentTableColumns(table);
        if (columns == null || columns.isEmpty()) {
            createTableAndIndex(table);
        } else {
            upgradeTable(table, columns);
        }
    }

    private Table getCurrentTableOrRename(Table table) {
        Table currentTable = queryTable(table);
        if (currentTable != null) {
            return currentTable;
        }
        if (!ObjectUtil.isEmpty(table.getOldTableNames())) {
            for (String oldTableName : table.getOldTableNames()) {
                Table oldTable = queryTable(new Table(oldTableName, table.getSchema()));
                if (oldTable != null) {
                    rename(oldTable, table);
                    return queryTable(table);
                }
            }
        }
        return null;
    }

    protected void modifyTableComment(Table table, Table oldTable) {
        if (table.getDisableAlterComment() == Boolean.TRUE) {
            return;
        }

        if (table.getComment() != null) {
            if (oldTable != null && !table.getComment().equals(oldTable.getComment())) {
                Sql sql = dialect.getSetTableCommentSql(table);
                runSql(sql);
            }
        }
    }

    @Override
    public void dropTable(Table table) {
        Sql sql = dialect.getDropTableSql(table);
        runSql(sql);
    }

    @Override
    public void rename(Table oldTable, Table newTable) {
        if (Objects.equals(oldTable.getTableName(), newTable.getTableName())) {
            return;
        }

        Sql sql = dialect.getRenameTableSql(oldTable, newTable);
        runSql(sql);
    }

    public Table queryTable(Table table) {
        return metaDataHelper.getTable(dialect.getTableNameInMeta(table), dialect.getSchemaNameInMeta(table));
    }

    public List<Column> getCurrentTableColumns(String tableName, String schemaName) {
        return getCurrentTableColumns(new Table(tableName, schemaName));
    }

    public List<Column> getCurrentTableColumns(Table table) {
        List<Column> columns = metaDataHelper.getColumns(dialect.getTableNameInMeta(table), dialect.getSchemaNameInMeta(table));
        List<Index> indexList = metaDataHelper.getIndexList(dialect.getTableNameInMeta(table), dialect.getSchemaNameInMeta(table));

        Map<String, Index> indexMap = new HashMap<>();
        indexList.stream().filter(t -> t.getColumnNames().size() == 1)
                .forEach(t -> indexMap.put(t.getColumnNames().get(0).toLowerCase(), t));

        for (Column c : columns) {
            dialect.normalizeColumn(c);

            Index index = indexMap.get(c.getColumnName());
            if (index != null) {
                // 过滤掉主键索引
                if (table.getPrimaryKeyColumns() == null || table.getPrimaryKeyColumns().stream().noneMatch(t -> t.equalsIgnoreCase(c.getColumnName()))) {
                    c.setIndex(true);
                    c.setIndexName(dialect.wrapper(index.getIndexName()));
                    c.setIndexType(index.getIndexType());
                }
            }

            c.setColumnName(dialect.wrapper(c.getColumnName()));
        }
        return columns;
    }

    protected void createTableAndIndex(Table table) {
        List<Sql> sqlList = new ArrayList<>(dialect.getCreateTableSql(table));

        table.getColumns().forEach(c -> {
            if (c.getAlterOrDropStrategy() != AlterOrDropStrategy.DROP) {
                getCreateIndexSql(table, c).ifPresent(sqlList::add);
            }
        });
        for (Sql sql : sqlList) {
            if (sql != null && sql.getSql() != null) {
                runSql(sql);
            }
        }
    }

    private void runSql(Sql sql) {
        if (sql.isIgnoreError()) {
            try {
                doRunSql(sql);
            } catch (Exception e) {
                String message = e.getMessage();
                if (!StringUtil.hasText(e.getMessage()) && e.getCause() != null) {
                    message = e.getCause().getMessage();
                }
                log.info("可忽略的sql执行错误: {},  message:{}", sql, message);
            }
        } else {
            doRunSql(sql);
        }
    }

    private void doRunSql(Sql sql) {
        log.info("EXEC DDL SQL: {}, ", sql.getSql());
        sqlHelper.update(null, CONST_SQL_EXP, Collections.singletonMap(CONST_SQL_KEY, sql.getSql()));
    }

    protected Optional<Sql> getCreateIndexSql(Table table, Column column) {
        if (column.isIndex() && (table.getPrimaryKeyColumns() == null || table.getPrimaryKeyColumns().stream().noneMatch(t -> t.equalsIgnoreCase(column.getColumnName())))) {
            String indexName = column.getIndexName();
            Assert.notNull(indexName, "indexName is null");
            return Optional.ofNullable(dialect.getAddIndexSql(table, column, indexName));
        }
        return Optional.empty();
    }

    /**
     * 更新表结构，只会增加列和修改列，不会删除列。
     */
    protected void upgradeTable(Table table, List<Column> currentColumns) {
        List<Sql> sqlList = new ArrayList<>();

        for (Column newColumn : table.getColumns()) {
            Column oldColumn = currentColumns.stream()
                    .filter(t -> t.getColumnName().equalsIgnoreCase(newColumn.getColumnName()))
                    .findFirst()
                    .orElseGet(() -> {
                                if (newColumn.getOldColumnNames() == null) {
                                    return null;
                                }
                                return newColumn.getOldColumnNames().stream()
                                        .map(oldName -> currentColumns.stream().filter(t -> t.getColumnName().equalsIgnoreCase(oldName)).findFirst().orElse(null))
                                        .filter(Objects::nonNull)
                                        .findFirst()
                                        .orElse(null);
                            }
                    );
            if (oldColumn == null) {
                if (newColumn.getAlterOrDropStrategy() != AlterOrDropStrategy.DROP) {
                    sqlList.addAll(getAddColumnAndIndexSql(table, newColumn));
                }
            } else {
                oldColumn.setChecked(true);
                if (newColumn.getAlterOrDropStrategy() == AlterOrDropStrategy.DROP) {
                    // 非改名的情况，才会删除列
                    if (oldColumn.getColumnName().equalsIgnoreCase(newColumn.getColumnName())) {
                        sqlList.add(dialect.getDropColumnSql(table, oldColumn));
                    }
                } else if (newColumn.getAlterOrDropStrategy() == AlterOrDropStrategy.DROP_AND_RECREATE) {
                    if (isColumnChanged(newColumn, oldColumn)) {
                        sqlList.add(dialect.getDropColumnSql(table, oldColumn));
                        sqlList.addAll(getAddColumnAndIndexSql(table, newColumn));
                    } else if (isColumnCommentChanged(table, newColumn, oldColumn)) {
                        sqlList.add(dialect.getSetColumnCommentSql(table, newColumn));
                    }
                } else if (newColumn.getAlterOrDropStrategy() == AlterOrDropStrategy.IGNORE) {
                    continue;
                } else {
                    sqlList.addAll(getAlterColumnSql(table, newColumn, oldColumn));
                }
            }
        }

        currentColumns.stream()
                .filter(t -> t.getAlterOrDropStrategy() != AlterOrDropStrategy.DROP)
                .filter(t -> t.getAlterOrDropStrategy() != AlterOrDropStrategy.IGNORE)
                .filter(t -> !t.isChecked())
                .forEach(t -> log.warn("undefined columns: '{}.{}'", table.getTableName(), t.getColumnName()));

        for (Sql sql : sqlList) {
            if (sql != null && sql.getSql() != null) {
                runSql(sql);
            }
        }
    }

    protected List<Sql> getAddColumnAndIndexSql(Table table, Column newColumn) {
        List<Sql> result = new ArrayList<>(dialect.getAddColumnSql(table, newColumn));
        getCreateIndexSql(table, newColumn).ifPresent(result::add);
        return result;
    }

    private boolean isColumnChanged(Column newColumn, Column oldColumn) {
        boolean nameEqual = newColumn.getColumnName().equalsIgnoreCase(oldColumn.getColumnName());
        boolean dataTypeEqual = newColumn.getDataType().equalsIgnoreCase(oldColumn.getDataType()) || (isInt(newColumn) && isInt(oldColumn)) || "USER-DEFINED".equals(oldColumn.getDataType());
        boolean lengthEqual = Objects.equals(newColumn.getCharacterMaximumLength(), oldColumn.getCharacterMaximumLength());
        boolean precisionEqual = newColumn.getNumericPrecision() == null || Objects.equals(newColumn.getNumericPrecision(), oldColumn.getNumericPrecision());
        boolean scaleEqual = Objects.equals(newColumn.getNumericScale() == null ? 0 : newColumn.getNumericScale(), oldColumn.getNumericScale() == null ? 0 : oldColumn.getNumericScale());
        boolean notNullEqual = newColumn.getNotNull() == null || newColumn.getNotNull() == oldColumn.getNotNull();
        boolean defaultValueEqual = Objects.equals(newColumn.getDefaultValue(), oldColumn.getDefaultValue());
        boolean vectorLengthEqual = oldColumn.getVectorLength() == null || newColumn.getVectorLength() == null || Objects.equals(newColumn.getVectorLength(), oldColumn.getVectorLength());
        return !(nameEqual && dataTypeEqual && lengthEqual && precisionEqual && scaleEqual && vectorLengthEqual && notNullEqual && defaultValueEqual);
    }

    private boolean isIndexChanged(Column newColumn, Column oldColumn) {
        if (!newColumn.isIndex() && !oldColumn.isIndex()) {
            return false;
        }

        boolean indexEqual = newColumn.isIndex() == oldColumn.isIndex();
        boolean NameEqual = Objects.equals(newColumn.getIndexName(), oldColumn.getIndexName());
        boolean TypeEqual = Objects.equals(newColumn.getIndexType(), oldColumn.getIndexType());
        boolean customIndexColumn = Objects.equals(newColumn.getCustomIndexColumn(), oldColumn.getCustomIndexColumn());
        return !(indexEqual && NameEqual && TypeEqual && customIndexColumn);
    }

    private boolean isInt(Column column) {
        String type = column.getDataType();
        return "INT".equalsIgnoreCase(type) || "INTEGER".equalsIgnoreCase(type);
    }

    private boolean isColumnCommentChanged(Table table, Column newColumn, Column oldColumn) {
        if (table.getDisableAlterComment() == Boolean.TRUE) {
            return false;
        }
        return newColumn.getComment() != null && !newColumn.getComment().equals(oldColumn.getComment());
    }

    protected List<Sql> getAlterColumnSql(Table table, Column newColumn, Column oldColumn) {
        List<Sql> result = new ArrayList<>();

        boolean isColumnChanged = isColumnChanged(newColumn, oldColumn);
        boolean isColumnCommentChanged;
        if (table.getDisableAlterComment() == Boolean.TRUE) {
            newColumn.setComment(oldColumn.getComment());
            isColumnCommentChanged = false;
        } else {
            isColumnCommentChanged = isColumnCommentChanged(table, newColumn, oldColumn);
        }

        if (isColumnChanged || isColumnCommentChanged) {
            if (isColumnChanged || dialect.isAlertColumnIncludeComment()) {
                result.addAll(dialect.getAlterColumnTypeSql(table, newColumn, oldColumn));
            }

            if (!dialect.isAlertColumnIncludeComment() && isColumnCommentChanged) {
                result.add(dialect.getSetColumnCommentSql(table, newColumn));
            }
        }

        boolean isIndexChanged = isIndexChanged(newColumn, oldColumn);
        if (isIndexChanged) {
            if (oldColumn.isIndex() && !newColumn.isIndex()) {
                result.add(dialect.getDropIndexSql(table, oldColumn.getIndexName()));
            } else if (!oldColumn.isIndex() && newColumn.isIndex()) {
                getCreateIndexSql(table, newColumn).ifPresent(result::add);
            } else if ((newColumn.getIndexType() == IndexType.NORMAL || newColumn.getIndexType() == IndexType.UNIQUE)
                    && newColumn.getIndexType() != oldColumn.getIndexType()) {
                result.add(dialect.getDropIndexSql(table, oldColumn.getIndexName()));
                getCreateIndexSql(table, newColumn).ifPresent(result::add);
            } else {
                log.warn("不支持索引变更: {} -> {}", oldColumn, newColumn);
                // TODO 暂不支持
            }
        }
        return result;
    }

//    private void sqlLog(String sql, Map<String, Object> context, Class<?> clazz) {
//        if (log.isDebugEnabled() && CONST_SQL_EXP.equals(sql)) {
//            log.debug("EXEC SQL: {}, params: {}, class:{}", context.get(CONST_SQL_KEY), sqlParamLog(context), clazz != null ? clazz.getSimpleName() : "null");
//        } else {
//            log.debug("EXEC SQL: {}, params: {}, class:{}", sql, context, clazz != null ? clazz.getSimpleName() : "null");
//        }
//    }
//
//    private StringBuilder sqlParamLog(Map<String, Object> context) {
//        return context.entrySet().stream()
//                .filter(e -> !e.getKey().equals(CONST_SQL_KEY))
//                .reduce(new StringBuilder(),
//                        (s, e) -> s.append(e.getKey()).append("=").append(e.getValue()).append(", "),
//                        (s1, s2) -> s1.append(", ").append(s2));
//    }

}
