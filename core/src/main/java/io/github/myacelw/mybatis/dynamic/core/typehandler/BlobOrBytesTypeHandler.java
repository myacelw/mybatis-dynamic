package io.github.myacelw.mybatis.dynamic.core.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.BlobTypeHandler;
import org.apache.ibatis.type.ByteArrayTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

/**
 * 数据库Blob或Bytes类型处理器
 *
 * @author liuwei
 */
public class BlobOrBytesTypeHandler extends BaseTypeHandler<byte[]> {
    public static BlobTypeHandler BLOB_TYPE_HANDLER = new BlobTypeHandler();
    public static ByteArrayTypeHandler BYTES_TYPE_HANDLER = new ByteArrayTypeHandler();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, byte[] parameter, JdbcType jdbcType) throws SQLException {
        BaseTypeHandler<byte[]> typeHandler = jdbcType == JdbcType.BLOB ? BLOB_TYPE_HANDLER : BYTES_TYPE_HANDLER;
        typeHandler.setNonNullParameter(ps, i, parameter, jdbcType);

    }

    @Override
    public byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int columnIndex = rs.findColumn(columnName);
        BaseTypeHandler<byte[]> typeHandler = Types.BLOB == rs.getMetaData().getColumnType(columnIndex) ? BLOB_TYPE_HANDLER : BYTES_TYPE_HANDLER;
        return typeHandler.getNullableResult(rs, columnName);
    }

    @Override
    public byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        BaseTypeHandler<byte[]> typeHandler = Types.BLOB == rs.getMetaData().getColumnType(columnIndex) ? BLOB_TYPE_HANDLER : BYTES_TYPE_HANDLER;
        return typeHandler.getNullableResult(rs, columnIndex);
    }

    @Override
    public byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        BaseTypeHandler<byte[]> typeHandler = Types.BLOB == cs.getMetaData().getColumnType(columnIndex) ? BLOB_TYPE_HANDLER : BYTES_TYPE_HANDLER;
        return typeHandler.getNullableResult(cs, columnIndex);
    }
}
