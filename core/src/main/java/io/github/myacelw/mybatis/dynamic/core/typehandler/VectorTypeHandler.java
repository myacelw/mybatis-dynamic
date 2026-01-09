package io.github.myacelw.mybatis.dynamic.core.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * float[] 类型处理器
 *
 * @author liuwei
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@Slf4j
public class VectorTypeHandler extends BaseTypeHandler<float[]> {
    private final static ObjectMapper objectMapper = JsonMapper.builder().build();

    @SneakyThrows
    protected float[] parse(String json) {
        return objectMapper.readValue(json, float[].class);
    }

    protected String toJson(float[] obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, toJson(parameter));
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String json = rs.getString(columnName);
        return StringUtil.hasText(json) ? parse(json) : null;
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final String json = rs.getString(columnIndex);
        return StringUtil.hasText(json) ? parse(json) : null;
    }

    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final String json = cs.getString(columnIndex);
        return StringUtil.hasText(json) ? parse(json) : null;
    }

}
