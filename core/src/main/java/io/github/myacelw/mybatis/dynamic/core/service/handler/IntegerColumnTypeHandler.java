package io.github.myacelw.mybatis.dynamic.core.service.handler;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.OracleDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;

/**
 * int类型字段转列类型处理器
 *
 * @author liuwei
 */
public class IntegerColumnTypeHandler implements ColumnTypeHandler{

    @Override
    public boolean doSetColumnType(Column column, Class<?> javaType, DataBaseDialect dialect, BasicField field) {
        if (javaType.equals(Integer.class) || javaType.equals(int.class)) {  //最大值：2147483647
            if(dialect instanceof OracleDataBaseDialect){
                column.setDataType("NUMERIC");
                column.setNumericPrecision(9);
                column.setNumericScale(0);
            }else{
                column.setDataType("INTEGER");
            }
            return true;
        }
        return false;
    }

}
