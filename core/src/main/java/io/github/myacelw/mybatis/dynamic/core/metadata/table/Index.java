package io.github.myacelw.mybatis.dynamic.core.metadata.table;

import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import lombok.Data;

import java.util.List;

/**
 * 数据库索引定义
 *
 * @author liuwei
 */
@Data
public class Index {

    List<String> columnNames;

    String indexName;

    /**
     * 索引类型
     */
    private IndexType indexType;

}
