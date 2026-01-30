package io.github.myacelw.mybatis.dynamic.core.metadata.table;

import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 查询返回列。
 * 参考自 org.apache.ibatis.mapping.ResultMapping
 *
 * @author liuwei
 */
@Data
public class SelectColumn implements Cloneable, Comparable<SelectColumn> {
    /**
     * 类型为ASSOCIATION 和 COLLECTION 时 为空
     */
    String column;

    String columnPrefix;

    String property;

    /**
     * 类型为ASSOCIATION 和 COLLECTION 时固定为Map.class
     */
    Class<?> javaType;

    TypeHandler<?> typeHandler;

    JdbcType jdbcType;

    Type type = Type.COLUMN;

    /**
     * 类型为ASSOCIATION 和 COLLECTION 时填写
     */
    List<SelectColumn> composites;

    /**
     * 是否为分组列
     */
    boolean groupBy;


    public static SelectColumn id(String column, String property, Class<?> javaType, Class<?> typeHandlerClass, JdbcType jdbcType) {
        SelectColumn s = column(column, property, javaType, null, typeHandlerClass, jdbcType);
        s.type = Type.ID;
        return s;
    }

    public static SelectColumn column(String column, String property, Class<?> javaType, Class<?> subJavaType, Class<?> typeHandlerClass, JdbcType jdbcType) {
        TypeHandler<?> typeHandler = null;
        if (typeHandlerClass != null) {
            typeHandler = TypeHandlerKey.getTypeHandler(javaType, subJavaType, typeHandlerClass);
        }
        return column(column, property, javaType, typeHandler, jdbcType);
    }

    public static SelectColumn column(String column, String property, Class<?> javaType, TypeHandler<?> typeHandler, JdbcType jdbcType) {
        Assert.notNull(column, "column can not be null");
        if (!StringUtil.hasText(property)) {
            property = unWrapper(column);
        }

        SelectColumn s = new SelectColumn();
        s.column = column;
        s.property = property;
        s.javaType = javaType == null ? Object.class : javaType;
        s.typeHandler = typeHandler;
        s.type = Type.COLUMN;
        s.jdbcType = jdbcType;
        return s;
    }

    private static String unWrapper(String column) {
        // fallback
        if (column == null || column.isEmpty()) {
            return column;
        }
        char first = column.charAt(0);
        char last = column.charAt(column.length() - 1);
        if ((first == '`' && last == '`') || (first == '"' && last == '"')) {
            return column.substring(1, column.length() - 1);
        }
        if (first == '[' && last == ']') {
            return column.substring(1, column.length() - 1);
        }
        return column;
    }

    public static SelectColumn association(String columnPrefix, String property, List<SelectColumn> composites, Class<?> javaType) {
        Assert.notNull(property, "property can not be null");

        SelectColumn s = new SelectColumn();
        s.columnPrefix = columnPrefix;
        s.property = property;
        s.javaType = javaType == null ? Map.class : javaType;
        s.type = Type.ASSOCIATION;
        s.composites = composites;
        return s;
    }

    public static SelectColumn collection(String columnPrefix, String property, List<SelectColumn> composites, Class<?> javaType) {
        Assert.notNull(property, "property can not be null");

        SelectColumn s = new SelectColumn();
        s.columnPrefix = columnPrefix;
        s.property = property;
        s.javaType = javaType == null ? Map.class : javaType;
        s.type = Type.COLLECTION;
        s.composites = composites;
        return s;
    }

    @Override
    public SelectColumn clone() {
        try {
            return (SelectColumn) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public int compareTo(SelectColumn o) {
        if (property == null && o.property == null) {
            return 0;
        }
        if (property == null) {
            return -1;
        }
        if (o.property == null) {
            return 1;
        }
        return this.property.compareTo(o.property);
    }

    public enum Type {

        COLUMN,

        ID,

        /**
         * 关联，对一关系
         */
        ASSOCIATION,

        /**
         * 关联，对多关系
         */
        COLLECTION;
    }

    @Value
    private static class TypeHandlerKey {
        Class<?> javaType;
        Class<?> subJavaType;
        Class<?> typeHandlerClass;

        static Map<TypeHandlerKey, TypeHandler<?>> typeHandlerCache = new ConcurrentHashMap<>();

        static TypeHandler<?> getTypeHandler(Class<?> javaType, Class<?> subJavaType, Class<?> typeHandlerClass) {
            TypeHandlerKey key = new TypeHandlerKey(javaType, subJavaType, typeHandlerClass);
            return typeHandlerCache.computeIfAbsent(key, TypeHandlerKey::getTypeHandler);
        }

        @SneakyThrows
        private TypeHandler<?> getTypeHandler() {
            Constructor<?> constructor0 = null;
            Constructor<?> constructor1 = null;
            Constructor<?> constructor2 = null;

            for (Constructor<?> constructor : typeHandlerClass.getConstructors()) {
                if (constructor.getParameterCount() == 0) {
                    constructor0 = constructor;
                } else if (constructor.getParameterCount() == 1 && constructor.getParameters()[0].getType() == Class.class) {
                    constructor1 = constructor;
                } else if (constructor.getParameterCount() == 2 && constructor.getParameters()[0].getType() == Class.class && constructor.getParameters()[1].getType() == Class.class) {
                    constructor2 = constructor;
                }
            }

            if (constructor2 != null) {
                return (TypeHandler<?>) constructor2.newInstance(javaType, subJavaType);
            } else if (constructor1 != null) {
                return (TypeHandler<?>) constructor1.newInstance(javaType);
            } else if (constructor0 != null) {
                return (TypeHandler<?>) constructor0.newInstance();
            }
            throw new IllegalArgumentException("No suitable constructor found for TypeHandler [" + typeHandlerClass + "]");
        }
    }

}
