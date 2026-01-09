package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.*;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.*;
import io.github.myacelw.mybatis.dynamic.core.typehandler.FloatArrayTypeHandler;
import io.github.myacelw.mybatis.dynamic.core.typehandler.VectorTypeHandler;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

/**
 * float[]类型字段转列类型处理器
 *
 * @author liuwei
 */
public class FloatArrayColumnTypeHandler implements ColumnTypeHandler {

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.isArray() && javaType.getComponentType().equals(float.class)) {
            if (dialect instanceof OceanBaseDataBaseDialect) {
                column.setDataType("VECTOR");
                column.setVectorLength(field.getExtPropertyValueForInteger(BasicField.EXT_PROPERTY_COLUMN_VECTOR_LENGTH, getDefaultEmbeddingVectorLength()));
                if (column.getTypeHandler() == null) {
                    column.setTypeHandler(VectorTypeHandler.class);
                    field.setTypeHandlerClass(VectorTypeHandler.class);
                }
                return true;
            } else if (dialect instanceof PostgresqlDataBaseDialect) {
                column.setDataType("BYTEA");
                column.setTypeHandler(VectorTypeHandler.class);
                field.setTypeHandlerClass(FloatArrayTypeHandler.class);
                return true;
            } else if (dialect instanceof OracleDataBaseDialect || dialect instanceof MysqlDataBaseDialect) {
                column.setDataType("BLOB");
                column.setTypeHandler(VectorTypeHandler.class);
                field.setTypeHandlerClass(FloatArrayTypeHandler.class);
                return true;
            }
//            else if (dialect instanceof MysqlDataBaseDialect) {
//                column.setDataTypeObj(DataType.VECTOR);
//                column.setVectorLength(field.getExtPropertyValueForInteger(BasicField.EXT_PROPERTY_COLUMN_VECTOR_LENGTH, getDefaultEmbeddingVectorLength()));
//                if (column.getTypeHandler() == null) {
//                    column.setTypeHandler(VectorTypeHandlerForMysql.class);
//                    field.putExtProperty(BasicField.EXT_PROPERTY_TYPE_HANDLER_CLASS, VectorTypeHandlerForMysql.class);
//                }
//                return true;
//            }
        }
        return false;
    }

    protected int getDefaultEmbeddingVectorLength() {
        int len = 1024;
        String lenStr = System.getenv("EMBEDDING_VECTOR_LENGTH");
        if (lenStr != null) {
            len = Integer.parseInt(lenStr);
        }
        return len;
    }
}
