package io.github.myacelw.mybatis.dynamic.core.metadata.enums;

/**
 * 字段对应的数据库列类型变化时的更新策略
 *
 * @author liuwei
 */
public enum AlterOrDropStrategy {
    /**
     * 进行列类型转换，如果更新失败则抛出异常；
     */
    ALTER,

    /**
     * 如果已经存在列，则不更新，如果不存在列仍然会创建列；可用于不希望修改列的情况；
     */
    IGNORE,

    /**
     * 删除并重建列，可用于列类型无法转换，但可清除列数据重建列的情况；
     */
    DROP_AND_RECREATE,

    /**
     * 删除列
     */
    DROP
}
