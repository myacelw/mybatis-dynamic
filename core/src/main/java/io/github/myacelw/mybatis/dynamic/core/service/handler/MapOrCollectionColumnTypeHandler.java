package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map或Collection类型字段转列类型处理器
 *
 * @author liuwei
 */
public class MapOrCollectionColumnTypeHandler implements ColumnTypeHandler{

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (Map.class.isAssignableFrom(javaType) || List.class.isAssignableFrom(javaType) || Set.class.isAssignableFrom(javaType)) {
            StringColumnTypeHandler.convertStringColumn(column, field, 65535, dialect);
            return true;
        }
        return false;
    }


}
