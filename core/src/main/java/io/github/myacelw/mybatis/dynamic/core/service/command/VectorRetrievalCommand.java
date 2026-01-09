package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 向量检索查询命令
 *
 * @author liuwei
 */
@Data
public class VectorRetrievalCommand<ID, T> implements Command {

    /**
     * 嵌入字段名
     */
    String embeddingField;

    /**
     * 排序
     */
    Integer topN;

    /**
     * 查询向量
     */
    float[] queryVector;

    /**
     * 查询返回的字段，为null时返回全部有权限的字段
     */
    List<String> selectFields;
    /**
     * 查询条件。
     * 对于树形查询是 树形查询结束后，关联join后的过滤条件
     */
    Condition condition;

    /**
     * 结果类型
     */
    Class<T> clazz;

    /**
     * 最大距离，用于过滤结果，如果不设置则不进行过滤
     */
    Double maxDistance;

    /**
     * 查询返回的距离字段名，如果为空则不返回距离字段
     */
    String selectDistanceFieldName;

    /**
     * 拷贝属性设置
     */
    public void copyProperties(VectorRetrievalCommand<ID, T> source) {
        setCondition(source.getCondition());
        setClazz(source.getClazz());
        setSelectFields(source.getSelectFields());
        setTopN(source.getTopN());
    }

    public static <ID> VectorRetrievalCommand<ID, Map<String, Object>> build() {
        return new VectorRetrievalCommand<>();
    }


}
