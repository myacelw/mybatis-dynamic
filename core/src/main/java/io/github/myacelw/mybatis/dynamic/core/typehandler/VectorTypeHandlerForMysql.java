package io.github.myacelw.mybatis.dynamic.core.typehandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * float[] 类型Mysql数据库处理器
 *
 * @author liuwei
 */
@MappedJdbcTypes(JdbcType.BINARY)
@Slf4j
public class VectorTypeHandlerForMysql extends BaseTypeHandler<float[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType) throws SQLException {
        ps.setBytes(i, floatArrayToBytes(parameter));
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return bytesToFloatArray(rs.getBytes(columnName));
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return bytesToFloatArray(rs.getBytes(columnIndex));
    }

    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return bytesToFloatArray(cs.getBytes(columnIndex));
    }

    /**
     * 将浮点数组转换为字节数组。
     *
     * @param floatArray 待转换的浮点数组
     * @return 转换后的字节数组
     */
    public static byte[] floatArrayToBytes(float[] floatArray) {
        int totalBytes = floatArray.length * Float.BYTES;
        // 注意Mysql要求使用LittleEndian，否则会导致数据不一致
        ByteBuffer byteBuffer = ByteBuffer.allocate(totalBytes).order(ByteOrder.LITTLE_ENDIAN);

        for (float f : floatArray) {
            if (Float.isNaN(f) || Float.isInfinite(f)) {
                throw new IllegalArgumentException("Illegal vector value: " + f);
            }
            byteBuffer.putFloat(f);
        }
        return byteBuffer.array();
    }

    /**
     * 将字节数组转换为浮点数组。
     *
     * @param byteArray 待转换的字节数组
     * @return 转换后的浮点数组
     */
    public static float[] bytesToFloatArray(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0) {
            return null;
        }

        // 使用小端字节序解析字节数组
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN);

        int length = byteArray.length / Float.BYTES;
        float[] floatArray = new float[length];
        for (int j = 0; j < length; j++) {
            floatArray[j] = byteBuffer.getFloat();
        }
        return floatArray;
    }


}
