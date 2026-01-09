package io.github.myacelw.mybatis.dynamic.core.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.StringTypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 抽象的加解密字符串类型处理器
 *
 * @author liuwei
 */
public abstract class AbstractSecretTypeHandler extends StringTypeHandler {

    /**
     * 加密
     */
    protected abstract String encrypt(String text);

    /**
     * 解密
     */
    protected abstract String decrypt(String text);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        super.setNonNullParameter(ps, i, encrypt(parameter), jdbcType);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        return decrypt(super.getNullableResult(rs, columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return decrypt(super.getNullableResult(rs, columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(super.getNullableResult(cs, columnIndex));
    }

}
