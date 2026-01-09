package io.github.myacelw.mybatis.dynamic.core.metadata.query;

import lombok.Value;

import java.util.List;

/**
 * 字段转换为SQL接口
 *
 * @author liuwei
 */
public interface FieldToSqlConverter {

    String convertColumn(String fieldName);

    default ConvertExistsSqlResult convertExistsSql(String fieldPath, List<Join> joins) {
        throw new UnsupportedOperationException("not support");
    }

    @Value
    class ConvertExistsSqlResult {
        String sql;
        FieldToSqlConverter converter;
    }

}
