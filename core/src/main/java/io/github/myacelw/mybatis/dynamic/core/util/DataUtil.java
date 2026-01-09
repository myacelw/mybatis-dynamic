package io.github.myacelw.mybatis.dynamic.core.util;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据操作工具类
 *
 * @author liuwei
 */
@Slf4j
public class DataUtil {

    /**
     * bean转换为Map
     */
    public static Map<String, Object> beanToMap(Object bean) {
        return (Map<String, Object>) toMapOrList(bean);
    }

    public static <T> T mapToBean(Object bean, Class<T> clazz) {
        return BeanUtil.toBean(bean, clazz);
    }

    /**
     * bean列表转换为Map列表
     */
    public static List<Map<String, Object>> beanListToListMap(Collection<?> beanList) {
        return (List<Map<String, Object>>) toMapOrList(beanList);
    }

    private static Object toMapOrList(Object bean) {
        if (bean == null) {
            return null;
        }
        Class<?> clazz = bean.getClass();
        if (bean instanceof Collection) {
            Collection<?> list = (Collection<?>) bean;
            return list.stream().map(DataUtil::toMapOrList).collect(Collectors.toList());
        } else if (bean instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) bean;
            return toMap(map);
        } else if (isBasicType(clazz)) {
            return bean;
        } else {
            Map<String, Object> map = BeanUtil.beanToMap(bean);
            return toMap(map);
        }
    }

    public static boolean isBasicType(Class<?> type) {
        return type != null && (type.isPrimitive() || type.isArray() || type.isEnum() || type.getName().startsWith("java"));
    }

    private static Map<Object, Object> toMap(Map<?, ?> map) {
        Map<Object, Object> result = new LinkedHashMap<>();
        map.forEach((k, v) -> result.put(k, toMapOrList(v)));
        return result;
    }

    public static boolean containsKey(Object data, String key) {
        if (data == null) {
            return false;
        }
        if (data instanceof Map) {
            return ((Map<?, ?>) data).containsKey(key);
        }
        return BeanUtil.containsProperty(data.getClass(), key);
    }

    public static Object getProperty(Object data, String key) {
        if (data == null) {
            return false;
        }
        if (data instanceof Map) {
            return ((Map<?, ?>) data).get(key);
        }
        return BeanUtil.getProperty(data, key);
    }

    public static void setProperty(Object data, String key, Object value) {
        if (data == null) {
            return;
        }
        if (data instanceof Map) {
            ((Map) data).put(key, value);
        } else {
            try {
                BeanUtil.setProperty(data, key, value);
            } catch (Exception e) {
                log.error("setProperty error, Object Class:{}, Key:{}, Value Class:{}", data.getClass(), key, value == null ? null : value.getClass());
                throw e;
            }
        }
    }

    public static void setPropertyIgnoreError(Object data, String key, Object value) {
        try {
            setProperty(data, key, value);
        } catch (Exception e) {
            log.debug("setPropertyIgnoreError error, Object Class:{}, Key:{}, Value Class:{}", data.getClass(), key, value == null ? null : value.getClass());
        }
    }

    /**
     * 构建树形结构数据
     *
     * @param beanOrMapList     bean或者Map数据列表
     * @param parentIdFieldNames   父节点Id字段名
     * @param childrenFieldName 存储子节点列表的字段名
     */
    public static <T> List<T> buildTree(Collection<T> beanOrMapList, String[] parentIdFieldNames, String childrenFieldName, String[] idFieldNames) {
        if (beanOrMapList == null || beanOrMapList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Object, T> map = new HashMap<>();
        for (T bean : beanOrMapList) {
            Object id = getValue(bean, idFieldNames);
            map.put(id, bean);
        }

        List<T> result = new ArrayList<>();
        for (T bean : beanOrMapList) {
            T parent = Optional.ofNullable(getValue(bean, parentIdFieldNames))
                    .map(map::get)
                    .orElse(null);

            if (parent != null) {
                List<T> children = (List<T>) getProperty(parent, childrenFieldName);
                if (children == null) {
                    children = new ArrayList<>();
                    setProperty(parent, childrenFieldName, children);
                }
                children.add(bean);
            } else {
                result.add(bean);
            }
        }

        return result;
    }

    private static Object getValue(Object bean, String[] fields) {
        if (fields.length == 1) {
            return getProperty(bean, fields[0]);
        }
        List<Object> result = new ArrayList<>();
        for (String field : fields) {
            Object v = getProperty(bean, field);
            if (v == null) {
                return null;
            }
            result.add(v);
        }
        return result;
    }

    /**
     * 得到ID值，参数允许为对象ID或含有ID属性的对象
     *
     * @param object 对象ID或含有ID属性的对象
     * @return 关联ID值
     */
    public static Object getIdValue(Object object, String idFieldName) {
        if (object == null || object instanceof String || object instanceof Integer || object instanceof Long) {
            return object;
        }
        return DataUtil.getProperty(object, idFieldName);
    }

    /**
     * 获取非空值 Map
     */
    public static Map<String, Object> getNoEmptyValueMap(Object bean) {
        Map<String, Object> result = new HashMap<>();

        if (bean == null) {
            return result;
        }

        Map<String, Object> map;

        if (bean instanceof Map) {
            map = (Map) bean;
        } else {
            map = BeanUtil.beanToMap(bean);
        }

        map.forEach((k, v) -> {
            if (v instanceof String) {
                if (StringUtil.hasText((String) v)) {
                    result.put(k, v);
                }
            } else if (!ObjectUtil.isEmpty(v)) {
                result.put(k, v);
            }
        });
        return result;
    }

    /**
     * 获取单值或多值
     */
    public static Object getSingleOrMultiValue(Object item, String[] fields) {
        if (fields.length == 1) {
            return DataUtil.getProperty(item, fields[0]);
        }

        List<Object> values = new ArrayList<>(fields.length);
        for (String field : fields) {
            Object value = DataUtil.getProperty(item, field);
            if (ObjectUtil.isEmpty(value)) {
                return null;
            }
            values.add(value);
        }
        return values;
    }

}
