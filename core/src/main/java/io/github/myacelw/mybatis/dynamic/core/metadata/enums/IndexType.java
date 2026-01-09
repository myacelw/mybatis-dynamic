package io.github.myacelw.mybatis.dynamic.core.metadata.enums;

/**
 * 索引类型
 *
 * @author liuwei
 */
public enum IndexType {
    /**
     * 普通索引
     */
    NORMAL,
    /**
     * 唯一索引
     */
    UNIQUE,

    /**
     * 全文检索索引
     */
    FULLTEXT,

    /**
     * 向量索引
     */
    VECTOR,
}
