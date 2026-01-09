package io.github.myacelw.mybatis.dynamic.core.metadata.field;

import io.github.myacelw.mybatis.dynamic.core.metadata.enums.ColumnAlterStrategy;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import lombok.Data;

import java.util.List;

/**
 * 列定义
 *
 * @author liuwei
 */
@Data
public class ColumnDefine {
    /**
     * 列类型
     */
    private String columnType;

    /**
     * 最大长度， 字符串类型有效
     */
    private Integer characterMaximumLength;

    /**
     * 数字类型有效，精度
     */
    private Integer numericPrecision;

    /**
     * 数字类型有效，小数位
     */
    private Integer numericScale;

    /**
     * 是否需要创建索引。
     */
    private Boolean index;

    /**
     * 索引名称，可空，默认会使用 {tableName}_{columnName} 格式
     */
    private String indexName;

    /**
     * 索引类型，默认为普通索引
     */
    private IndexType indexType;

    /**
     * 自定义索引列，比如：json多值索引， (CAST($COL->'$[*]' AS char(512) ARRAY))
     */
    private String customIndexColumn;

    /**
     * 默认值。
     * 1、插入数据时如果没有赋值，则会使用默认值。
     * 2、表格插入新列时会将老数据的此列初始化为默认值。
     */
    private String defaultValue;

    /**
     * 是否非空
     */
    private Boolean notNull;

    /**
     * 列创建附加DDL语句， 例如： GENERATED ALWAYS AS (TRIM(BOTH '/' FROM REGEXP_SUBSTR(location, '[^/]+', 1, 1))) STORED
     */
    private String additionalDDl;

    /**
     * 从旧列名迁移数据时，需要指定旧列名。
     */
    private List<String> oldColumnNames;

    /**
     * 列备注
     */
    private String comment;

    /**
     * 是否禁用列注释变更
     */
    protected Boolean disableAlterComment;

    /**
     * 列更新策略
     */
    private ColumnAlterStrategy alterOrDropStrategy;

}
