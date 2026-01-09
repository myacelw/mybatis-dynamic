package io.github.myacelw.mybatis.dynamic.core.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Jackson 实现 JSON 字段类型处理器。
 * 支持Collection类型的泛型的处理。Map类型key固定为String类型，value支持泛型处理。
 *
 * @author liuwei
 */
@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JsonTypeHandler extends BaseTypeHandler<Object> {
    private static ObjectMapper OBJECT_MAPPER;

    private final Class<?> type;

    private final Class<?> subType;

    public JsonTypeHandler(Class<?> type) {
        this(type, null);
    }

    public JsonTypeHandler(Class<?> type, Class<?> subType) {
        if (log.isTraceEnabled()) {
            log.trace("JacksonTypeHandler(" + type + ")");
        }
        Assert.notNull(type, "Type argument cannot be null");
        this.type = type;
        this.subType = subType;
    }

    protected Object parse(String json) {
        try {
            if (subType != null) {
                if (List.class.isAssignableFrom(type)) {
                    CollectionType javaType = getObjectMapper().getTypeFactory().constructCollectionType((Class<? extends Collection>) type, subType);
                    return getObjectMapper().readValue(json, javaType);
                } else if (Map.class.isAssignableFrom(type)) {
                    MapType javaType = getObjectMapper().getTypeFactory().constructMapType((Class<? extends Map>) type, String.class, subType);
                    return getObjectMapper().readValue(json, javaType);
                }
            }
            if (type.isArray()) {
                ArrayType javaType = getObjectMapper().getTypeFactory().constructArrayType(subType != null ? subType : type.getComponentType());
                return getObjectMapper().readValue(json, javaType);
            }
            return getObjectMapper().readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String toJson(Object obj) {
        try {
            return getObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectMapper getObjectMapper() {
        if (null == OBJECT_MAPPER) {
            OBJECT_MAPPER = JsonMapper.builder().addModule(new JavaTimeModule()).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
        }
        return OBJECT_MAPPER;
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper should not be null");
        JsonTypeHandler.OBJECT_MAPPER = objectMapper;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, toJson(parameter));
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String json = rs.getString(columnName);
        return StringUtil.hasText(json) ? parse(json) : null;
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final String json = rs.getString(columnIndex);
        return StringUtil.hasText(json) ? parse(json) : null;
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final String json = cs.getString(columnIndex);
        return StringUtil.hasText(json) ? parse(json) : null;
    }

}
