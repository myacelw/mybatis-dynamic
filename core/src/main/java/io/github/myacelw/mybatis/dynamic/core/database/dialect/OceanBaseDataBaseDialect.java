package io.github.myacelw.mybatis.dynamic.core.database.dialect;

import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.Sql;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * OceanBase数据库方言
 *
 * @author liuwei
 */
public class OceanBaseDataBaseDialect extends MysqlDataBaseDialect {

    public final static String EXT_PROPERTY_TABLE_GROUP = "TABLE_GROUP";

    /**
     * 列存储类型
     */
    public final static String EXT_PROPERTY_COLUMN_STORAGE = "COLUMN_STORAGE";


    @Override
    public String getName() {
        return "oceanbase";
    }

    public int getPriority() {
        return 50;
    }

    @Override
    public List<Sql> getCreateTableSql(Table table) {
        String tableGroup = table.getExtPropertyValueForString(EXT_PROPERTY_TABLE_GROUP);
        Boolean columnStorage = table.getExtPropertyValueForBoolean(EXT_PROPERTY_COLUMN_STORAGE);

        if (!StringUtil.hasText(tableGroup)) {
            return super.getCreateTableSql(table);
        }

        List<Sql> sqlList = new ArrayList<>();
        sqlList.add(new Sql("CREATE TABLEGROUP " + tableGroup + " SHARDING = 'ADAPTIVE'", true));

        List<Sql> sql2 = super.getCreateTableSql(table);
        if (columnStorage == Boolean.TRUE) {
            Sql sql = sql2.get(0);
            sqlList.add(new Sql(sql.getSql() + " WITH COLUMN GROUP (each column)", sql.isIgnoreError()));
        } else {
            sqlList.addAll(sql2);
        }
        return sqlList;
    }

    @Override
    protected String getCreateTableSqlOthers(Table table) {
        String tableGroup = table.getExtPropertyValueForString(EXT_PROPERTY_TABLE_GROUP);
        if (StringUtil.hasText(tableGroup)) {
            return "TABLEGROUP = " + tableGroup;
        }
        return "";
    }

    @Override
    public Sql getAddIndexSql(Table table, Column column, @NonNull String indexName) {
        Sql sql = super.getAddIndexSql(table, column, indexName);
        if (column.getIndexType() == IndexType.VECTOR) {
            return new Sql(sql.getSql() + " WITH (distance=l2, type=hnsw)", true);
        } else if (column.getIndexType() == IndexType.FULLTEXT) {
            return new Sql(sql.getSql() + " WITH PARSER ik PARSER_PROPERTIES=(ik_mode=\"max_word\")", true);
        }
        return sql;
    }
}
