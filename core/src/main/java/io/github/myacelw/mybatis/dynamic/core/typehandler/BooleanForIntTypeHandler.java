package io.github.myacelw.mybatis.dynamic.core.typehandler;

import org.apache.ibatis.type.BooleanTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * int转boolean处理器
 *
 * @author liuwei
 */
public class BooleanForIntTypeHandler extends BooleanTypeHandler {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setInt(i, parameter == Boolean.TRUE ? 1 : 0);
    }
}
