package io.github.myacelw.mybatis.dynamic.core.util;

import io.github.myacelw.mybatis.dynamic.core.ext.ExtBean;
import lombok.SneakyThrows;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean工具
 *
 * @author liuwei
 */
public class BeanUtil {
    private static final Map<Class<?>, PropertyDescriptor[]> PD_CACHE = new ConcurrentHashMap<>();

    /**
     * 将bean的部分属性转换成map<br>
     * 可选拷贝哪些属性值，默认是不忽略值为{@code null}的值的。
     *
     * @param bean bean
     * @return Map
     * @since 5.8.0
     */
    @SneakyThrows
    public static Map<String, Object> beanToMap(Object bean) {
        if (null == bean) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();

        // ExtBean 需要把 ext map 中的值，直接拷贝到结果map中
        if (bean instanceof ExtBean) {
            Map<String, Object> ext = ((ExtBean) bean).getExt();
            result.putAll(ext);
        }

        for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass())) {
            String name = pd.getName();
            if (pd.getReadMethod() != null && !pd.getName().equals("class") && !(bean instanceof ExtBean && name.equals(ExtBean.NAME))) {
                result.put(name, pd.getReadMethod().invoke(bean));
            }
        }
        return result;
    }

    @SneakyThrows
    public static <T> T toBean(Object bean, Class<T> clazz) {
        if (null == bean) {
            return null;
        }

        if (clazz.isAssignableFrom(bean.getClass())) {
            return (T) bean;
        }

        if (bean instanceof Map) {
            T result = clazz.getConstructor().newInstance();
            setValue(clazz, (Map<?, ?>) bean, result);
        }
        throw new IllegalArgumentException("Unable to convert to bean");
    }

    @SneakyThrows
    private static <T> void setValue(Class<T> clazz, Map<?, ?> map, T result) {
        Map<String, Method> methods = getMethods(clazz);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            Method method = methods.get(key);
            if (method != null) {
                method.invoke(result, value);
            } else if (result instanceof ExtBean) {
                ((ExtBean) result).getExt().put(key, value);
            }
        }
    }

    @SneakyThrows
    private static Map<String, Method> getMethods(Class<?> clazz) {
        PropertyDescriptor[] pds = getPropertyDescriptors(clazz);
        Map<String, Method> methods = new HashMap<>();
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null) {
                methods.put(pd.getName(), pd.getWriteMethod());
            }
        }
        return methods;
    }

    @SneakyThrows
    public static boolean containsProperty(Class<?> clazz, String propertyName) {
        if (clazz == null) {
            return false;
        }
        if (ExtBean.class.isAssignableFrom(clazz)) {
            return true;
        }
        for (PropertyDescriptor pd : getPropertyDescriptors(clazz)) {
            if (pd.getName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    @SneakyThrows
    public static Object getProperty(Object bean, String propertyName) {
        String[] ps = propertyName.split("[.\\[\\]]");

        Object result = bean;
        for (String p : ps) {
            if (result == null) {
                return null;
            }
            if (p.isEmpty()) {
                continue;
            }
            if (result instanceof Map) {
                result = getProperty0((Map<?, ?>) result, p);
            } else if (result instanceof List) {
                result = getProperty0((List<?>) result, p);
            } else {
                result = getProperty0(result, p);
            }
        }
        return result;
    }

    @SneakyThrows
    private static Object getProperty0(Object bean, String propertyName) {
        for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass())) {
            if (pd.getReadMethod() != null && pd.getName().equals(propertyName)) {
                return pd.getReadMethod().invoke(bean);
            }
        }
        if (bean instanceof ExtBean) {
            return ((ExtBean) bean).getExt().get(propertyName);
        }
        return null;
    }

    private static Object getProperty0(Map<?, ?> bean, String propertyName) {
        return bean.get(propertyName);
    }

    private static Object getProperty0(List<?> bean, String propertyName) {
        int i = Integer.parseInt(propertyName);
        return bean.get(i);
    }


    @SneakyThrows
    public static void setProperty(Object bean, String propertyName, Object value) {
        if (bean == null) {
            return;
        }
        for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass())) {
            if (pd.getWriteMethod() != null && pd.getName().equals(propertyName)) {
                pd.getWriteMethod().invoke(bean, value);
                return;
            }
        }
        if (bean instanceof ExtBean) {
            ((ExtBean) bean).getExt().put(propertyName, value);
        }
    }

    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) {
        return PD_CACHE.computeIfAbsent(clazz, c -> {
            try {
                return Arrays.stream(Introspector.getBeanInfo(c).getPropertyDescriptors()).filter(pd->!pd.getName().equals("class")).toArray(PropertyDescriptor[]::new);
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() == Object.class) {
                return null;
            }
            return getField(clazz.getSuperclass(), fieldName);
        }
    }

    @SneakyThrows
    public static Class<?> getClassForName(String name) {
        return Class.forName(name);
    }

}