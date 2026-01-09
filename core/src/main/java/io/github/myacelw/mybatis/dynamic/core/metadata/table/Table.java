package io.github.myacelw.mybatis.dynamic.core.metadata.table;

import io.github.myacelw.mybatis.dynamic.core.metadata.enums.KeyGeneratorMode;
import io.github.myacelw.mybatis.dynamic.core.metadata.partition.Partition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.ExtProperties;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据库表结构定义
 *
 * @author liuwei
 */
@Data
public class Table implements ExtProperties {

    private final String tableName;

    private final String schema;

    /**
     * 旧的表名，用于从旧表迁移数据
     */
    private List<String> oldTableNames;

    private List<Column> columns = new ArrayList<>();

    private Partition partition;

    /**
     * 列备注
     */
    private String comment;

    private List<String> primaryKeyColumns;

    /**
     * 主键生成策略
     */
    private KeyGeneratorMode keyGeneratorMode;

    /**
     * 主键生成列
     */
    private String keyGeneratorColumn;

    /**
     * Key生成序列名称
     */
    private String keyGeneratorSequenceName;

    /**
     * 是否禁用表注释变更
     */
    protected Boolean disableAlterComment;

    /**
     * 扩展属性
     */
    private Map<String, Object> extProperties;

    public String getSchemaAndTableName() {
        return StringUtil.hasText(schema) ? (schema + "." + tableName) : tableName;
    }

    public Column getKeyGeneratorColumnObj() {
        return columns.stream().filter(column -> column.getColumnName().equals(keyGeneratorColumn)).findFirst().orElse(null);
    }


    public Table(String tableName, String schema) {
        this.tableName = tableName;
        this.schema = schema;
    }


}
