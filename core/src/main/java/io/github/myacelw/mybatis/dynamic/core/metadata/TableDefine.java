package io.github.myacelw.mybatis.dynamic.core.metadata;

import io.github.myacelw.mybatis.dynamic.core.metadata.partition.Partition;
import lombok.Data;

import java.util.List;

/**
 * 表定义
 *
 * @author liuwei
 */
@Data
public class TableDefine {

    /**
     * 表分区配置
     */
    private Partition partition;

    /**
     * 表备注
     */
    private String comment;

    /**
     * Key生成序列名称
     */
    private String keyGeneratorSequenceName;

    /**
     * 旧的表名，用于从旧表迁移数据
     */
    private List<String> oldTableNames;

    /**
     * 模型变更时指定删除的列名List，用于Drop不再使用的列
     */
    private List<String> dropColumnNames;

    /**
     * 是否禁用表结构更新，开启后将不会更新表结构
     */
    private Boolean disableTableCreateAndAlter;

    /**
     * 是否禁用表注释变更
     */
    private Boolean disableAlterComment;


}
