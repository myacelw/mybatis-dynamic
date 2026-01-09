package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;

/**
 * 字段按类型转列类型处理器
 *
 * @author liuwei
 */
public interface ColumnTypeHandler {

    /**
     * 字段转换为列类型
     */
    boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field);

}
