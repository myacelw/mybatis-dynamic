package io.github.myacelw.mybatis.dynamic.core.typehandler;

import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class 类型处理器
 *
 * @author liuwei
 */
public class ClassTypeHandler extends BaseTypeHandler<Class<?>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Class parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.getName());
    }

    @Override
    public Class<?> getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        String v = rs.getString(columnName);
        return stringToClass(v);
    }

    @Override
    public Class<?> getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        String v = rs.getString(columnIndex);
        return stringToClass(v);
    }

    @Override
    public Class<?> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String v = cs.getString(columnIndex);
        return stringToClass(v);
    }

    protected Class<?> stringToClass(String v) throws SQLException {
        if(StringUtil.hasText(v)) {
            try {
                return Class.forName(v);
            } catch (ClassNotFoundException e) {
                throw new SQLException(e.getMessage(), e);
            }
        }
        return null;
    }

}
