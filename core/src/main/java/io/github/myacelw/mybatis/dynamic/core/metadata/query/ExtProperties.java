package io.github.myacelw.mybatis.dynamic.core.metadata.query;

import io.github.myacelw.mybatis.dynamic.core.util.BeanUtil;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 扩展属性接口
 *
 * @author liuwei
 */
public interface ExtProperties {

    /**
     * 任意扩展属性
     */
    Map<String, Object> getExtProperties();

    void setExtProperties(Map<String, Object> extProperties);

    default boolean containsExtPropertyKey(String key) {
        if (getExtProperties() == null) {
            return false;
        }
        return getExtProperties().containsKey(key);
    }

    /**
     * 得到任意扩展属性值
     */
    default Object getExtProperty(String key) {
        if (getExtProperties() == null) {
            return null;
        }
        return getExtProperties().get(key);
    }

    default void putExtProperty(String key, Object value) {
        if (getExtProperties() == null) {
            setExtProperties(new LinkedHashMap<>());
        }
        getExtProperties().put(key, value);
    }

    default <T> T getExtProperty(@NonNull String key, @NonNull Class<T> clazz) {
        Object custom = getExtProperty(key);
        if (custom != null) {
            if (clazz.isAssignableFrom(custom.getClass())) {
                return (T) custom;
            } else {
                throw new IllegalArgumentException("Type error for extension property [" + key + "] with value [" + custom + "], expected " + clazz);
            }
        }
        return null;
    }

    default <T> T getExtPropertyValue(@NonNull String key, @NonNull Class<T> clazz, @NonNull Function<String, T> func) {
        Object custom = getExtProperty(key);
        if (custom != null) {
            try {
                if (clazz.isAssignableFrom(custom.getClass())) {
                    return (T) custom;
                }
                return func.apply(custom.toString());
            } catch (Exception e) {
                throw new IllegalArgumentException("Type error for extension property [" + key + "] with value '" + custom + "', cannot convert to " + clazz.getSimpleName());
            }
        }
        return null;
    }

    default String getExtPropertyValueForString(String key) {
        return getExtPropertyValue(key, String.class, t -> t);
    }


    default Integer getExtPropertyValueForInteger(String key) {
        return getExtPropertyValue(key, Integer.class, Integer::parseInt);
    }

    default Integer getExtPropertyValueForInteger(String key, Integer defaultValue) {
        Integer result = getExtPropertyValue(key, Integer.class, Integer::parseInt);
        return result == null ? defaultValue : result;
    }

    default Long getExtPropertyValueForLong(String key) {
        return getExtPropertyValue(key, Long.class, Long::parseLong);
    }

    default Boolean getExtPropertyValueForBoolean(String key) {
        return getExtPropertyValue(key, Boolean.class, Boolean::parseBoolean);
    }

    default <T> Class<T> getExtPropertyValueForClass(String key) {
        return getExtPropertyValue(key, Class.class, BeanUtil::getClassForName);
    }

    default <E extends Enum<E>> E getExtPropertyValueForEnum(String key, Class<E> enumClass) {
        Object v = getExtProperty(key);
        if (v.getClass().isEnum()) {
            return (E) v;
        }
        if (v instanceof Integer) {
            return enumClass.getEnumConstants()[(Integer) v];
        }
        if (v instanceof String) {
            return Enum.valueOf(enumClass, (String) v);
        }
        throw new IllegalArgumentException("Type error for extension property [" + key + "] with value [" + v + "], cannot convert to " + enumClass.getSimpleName());
    }

}
