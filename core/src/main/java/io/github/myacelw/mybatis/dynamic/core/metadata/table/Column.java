package io.github.myacelw.mybatis.dynamic.core.metadata.table;

import io.github.myacelw.mybatis.dynamic.core.metadata.enums.ColumnAlterStrategy;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import lombok.Data;
import org.apache.ibatis.type.TypeHandler;

import java.util.List;

/**
 * 数据表列定义
 *
 * @author liuwei
 */
@Data
public class Column {

    private String columnName;

    private String dataType;

    /**
     * 字符类型的最大长度
     */
    private Integer characterMaximumLength;

    /**
     * NUMERIC 类型的精度
     */
    private Integer numericPrecision;

    /**
     * NUMERIC 类型的小数位数
     */
    private Integer numericScale;

    /**
     * VECTOR 类型的向量长度
     */
    private Integer vectorLength;

    /**
     * 自定义索引列，比如：json多值索引， (CAST($COL->'$[*]' AS char(512) ARRAY))
     */
    private String customIndexColumn;

    /**
     * 是否非空
     */
    private Boolean notNull;

    /**
     * 列的默认值定义，如果是固定字符串，应使用单引号包裹
     */
    private String defaultValue;

    /**
     * 附加DDL语句
     */
    private String additionalDDl;

    /**
     * 列备注
     */
    private String comment;

    /**
     * 是否自动递增
     */
    private boolean autoIncrement;

    /**
     * 是否为生成列
     */
    private boolean generatedColumn;

    /**
     * 从旧列名迁移数据时，需要指定旧列名。
     */
    private List<String> oldColumnNames;

    /**
     * 是否需要索引
     */
    private boolean index;

    /**
     * 索引名
     */
    private String indexName;

    /**
     * 索引类型
     */
    private IndexType indexType;

    /**
     * 是否禁用列注释变更
     */
    protected Boolean disableAlterComment;

    /**
     * 列更新策略
     */
    private ColumnAlterStrategy alterOrDropStrategy;

    /**
     * 类型转换处理器
     */
    private Class<? extends TypeHandler> typeHandler;

//    @JsonIgnore
//    public DataType getDataTypeObj(){
//        try {
//            return DataType.valueOf(this.dataType);
//        }catch(Exception e){
//            return null;
//        }
//    }
//
//    @JsonIgnore
//    public void setDataTypeObj(DataType type){
//        this.dataType = type==null? null: type.toString();
//    }

    /**
     * 存在模型列的定义
     */
    private boolean checked;

    /**
     * 获取列的数据类型定义
     *
     * @return 列的数据类型定义字符串
     */
    public String getDataTypeDefinition() {
        // 使用 StringBuilder 来构建 SQL 语句
        StringBuilder sql = new StringBuilder(getDataType());
        // 如果字符最大长度不为空，则添加到 SQL 语句中
        if (getCharacterMaximumLength() != null) {
            sql.append("(").append(getCharacterMaximumLength()).append(")");
        }
        // 如果数值精度和小数位数都不为空，则添加到 SQL 语句中
        else if (getNumericPrecision() != null && getNumericScale() != null) {
            sql.append("(").append(getNumericPrecision()).append(",").append(getNumericScale()).append(")");
        }
        // 如果只有数值精度不为空，则添加到 SQL 语句中
        else if (getNumericPrecision() != null) {
            sql.append("(").append(getNumericPrecision()).append(")");
        } else if (getVectorLength() != null) {
            sql.append("(").append(getVectorLength()).append(")");
        }
        // 返回构建好的 SQL 语句
        return sql.toString();
    }


}
