package io.github.myacelw.mybatis.dynamic.core.metadata.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.AlterOrDropStrategy;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.IndexType;
import io.github.myacelw.mybatis.dynamic.core.service.ModelToTableConverter;
import io.github.myacelw.mybatis.dynamic.core.typehandler.BooleanForIntTypeHandler;
import io.github.myacelw.mybatis.dynamic.core.typehandler.JsonTypeHandler;
import io.github.myacelw.mybatis.dynamic.core.util.BeanUtil;
import io.github.myacelw.mybatis.dynamic.core.util.DataUtil;
import lombok.*;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基本类型字段
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class BasicField extends AbstractField implements Serializable, Field {

    private static final long serialVersionUID = 1L;

    /**
     * Basic类型时，并且javaClass 为 Map、List、Set 时，泛型参数类型
     */
    private Class<?> javaParameterClass;

    /**
     * 字段对应列指定jdbcType
     */
    private JdbcType jdbcType;

    /**
     * 字段对应列指定typeHandlerClass，类型转换处理器类.
     */
    private Class<? extends TypeHandler> typeHandlerClass;

    /**
     * 数据库列名
     */
    protected String columnName;

    /**
     * 列定义
     */
    private ColumnDefinition columnDefinition = new ColumnDefinition();

    /**
     * 字段值自动填充器，可空
     */
    private String fillerName;

    /**
     * 是否需要查询
     */
    private Boolean select = true;

    @Override
    public void init(Model model, ModelToTableConverter converter) {
        init(model, converter, "");
    }

    void init(Model model, ModelToTableConverter converter, String propertyPrefix) {
        if (typeHandlerClass == null) {
            Class<? extends TypeHandler> defaultTypeHandler = getDefaultTypeHandlerClass();
            if (defaultTypeHandler != null) {
                typeHandlerClass = defaultTypeHandler;
            }
        }
        columnName = converter.getColumnName(propertyPrefix + getName(), columnName);

        if (columnDefinition.getIndex() == Boolean.TRUE) {
            columnDefinition.setIndexName(converter.getIndexName(model.getTableName(), columnName, columnDefinition.getIndexName()));
        }

        if (columnDefinition.getOldColumnNames() != null) {
            columnDefinition.setOldColumnNames(
                    columnDefinition.getOldColumnNames().stream()
                            .map(converter::getWrappedIdentifierInMeta)
                            .collect(Collectors.toList())
            );
        }
    }

    @JsonIgnore
    public void setComment(String comment) {
        this.columnDefinition.setComment(comment);
    }

    private Class<? extends TypeHandler> getDefaultTypeHandlerClass() {
        //TODO 以后类型可以为可配置的，而不是写死
        if (Map.class.isAssignableFrom(javaClass) || List.class.isAssignableFrom(javaClass) || Set.class.isAssignableFrom(javaClass)) {
            return JsonTypeHandler.class;
        } else if (Boolean.class == javaClass) {
            return BooleanForIntTypeHandler.class;
        }
//        else if (byte[].class == javaClass) {
//            return BlobOrBytesTypeHandler.class;
//        } else if (Class.class == javaClass) {
//            return ClassTypeHandler.class;
//        }
        return null;
    }

    public void clearTableAndColumnName() {
        columnName = null;
    }

    @Override
    public BasicField clone() {
        return (BasicField) super.clone();
    }

    @Override
    public Object sampleData() {
        return sampleData(javaClass, javaParameterClass, true);
    }

    private Object sampleData(Class<?> javaClass, Class<?> javaParameterClass, boolean recurse) {
        if (javaClass == null) {
            return null;
        }
        Object value = null;
        if (List.class.isAssignableFrom(javaClass)) {
            Object item = sampleData(javaParameterClass, null, recurse);
            value = item == null ? Collections.emptyList() : Collections.singletonList(item);
        } else if (Set.class.isAssignableFrom(javaClass)) {
            Object item = sampleData(javaParameterClass, null, recurse);
            value = item == null ? Collections.emptySet() : Collections.singleton(item);
        } else if (Map.class.isAssignableFrom(javaClass)) {
            Object item = sampleData(javaParameterClass, null, recurse);
            value = item == null ? Collections.emptyMap() : Collections.singletonMap("<key>", item);
        } else if (javaClass.isArray()) {
            Class<?> type = javaParameterClass != null ? javaParameterClass : javaClass.getComponentType();
            Object item = sampleData(type, null, recurse);
            if (item != null) {
                value = Array.newInstance(type, 1);
                Array.set(value, 0, item);
            } else {
                value = Array.newInstance(type, 0);
            }
        } else if (Enum.class.isAssignableFrom(javaClass)) {
            Object[] vs = javaClass.getEnumConstants();
            value = vs.length > 0 ? vs[0] : null;
        } else if (DataUtil.isBasicType(javaClass)) {
            value = BASIC_SAMPLE_VALUE.get(javaClass);
        } else if (recurse) {
            value = sampleDataForBean(javaClass);
        }
        return value;
    }

    private Map<String, Object> sampleDataForBean(Class<?> javaClass) {
        Map<String, Object> map = new LinkedHashMap<>();
        PropertyDescriptor[] pds = BeanUtil.getPropertyDescriptors(javaClass);
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null) {
                map.put(pd.getName(), sampleData(pd.getPropertyType(), null, false));
            }
        }
        return map.isEmpty() ? null : map;
    }

    private final static Map<Class<?>, Object> BASIC_SAMPLE_VALUE = new HashMap<Class<?>, Object>() {
        {
            put(String.class, "String");
            put(Integer.class, 0);
            put(Long.class, 0L);
            put(Float.class, 0F);
            put(Double.class, 0D);
            put(Boolean.class, false);
            put(LocalDate.class, LocalDate.now());
            put(LocalDateTime.class, LocalDateTime.now());
            put(BigInteger.class, BigInteger.ZERO);
            put(BigDecimal.class, BigDecimal.ZERO);
            put(byte.class, (byte) 'b');
            put(char.class, 'c');
            put(short.class, 0);
            put(int.class, 0);
            put(long.class, 0L);
            put(float.class, 0F);
            put(double.class, 0D);
            put(boolean.class, false);
        }
    };


    public static class BuilderAndPrecisionAndScale extends Builder<BuilderAndPrecisionAndScale> {

        public BuilderAndPrecisionAndScale(String name, Class<?> javaType) {
            super(name, javaType);
        }

        public BuilderAndPrecisionAndScale precision(int precision) {
            field.columnDefinition.setNumericPrecision(precision);
            return this;
        }

        public BuilderAndPrecisionAndScale scale(int scale) {
            field.columnDefinition.setNumericScale(scale);
            return this;
        }
    }


    public static class BuilderAndMaxLength extends Builder<BuilderAndMaxLength> {

        public BuilderAndMaxLength(String name, Class<?> javaType) {
            super(name, javaType);
        }

        public BuilderAndMaxLength characterMaximumLength(Integer characterMaximumLength) {
            field.columnDefinition.setCharacterMaximumLength(characterMaximumLength);
            return this;
        }
    }


    public static class Builder<T extends Builder<?>> extends AbstractField.Builder<BasicField, T> {

        public Builder(String name, Class<?> javaType) {
            super(name, new BasicField());
            this.field.setJavaClass(javaType);
        }

        public Builder(String name, String javaType) {
            this(name, convertToClass(javaType));
        }


        public Builder(String name, BasicField field) {
            super(name, field);
        }

        public T columnName(String columnName) {
            field.setColumnName(columnName);
            return self();
        }

        public T javaParameterClass(Class<?> javaParameterClass) {
            field.setJavaParameterClass(javaParameterClass);
            return self();
        }

        public T javaParameterClass(String javaParameterClass) {
            field.setJavaParameterClass(convertToClass(javaParameterClass));
            return self();
        }

        public T jdbcType(JdbcType jdbcType) {
            field.setJdbcType(jdbcType);
            return self();
        }

        public T typeHandlerClass(Class<? extends TypeHandler> typeHandlerClass) {
            field.setTypeHandlerClass(typeHandlerClass);
            return self();
        }

        public T select(Boolean select) {
            field.setSelect(select);
            return self();
        }

        public T fillerName(String fillerName) {
            field.setFillerName(fillerName);
            return self();
        }

        public T ddlColumnType(String columnType) {
            field.columnDefinition.setColumnType(columnType);
            return self();
        }

        public T ddlCharacterMaximumLength(Integer characterMaximumLength) {
            field.columnDefinition.setCharacterMaximumLength(characterMaximumLength);
            return self();
        }

        public T ddlNumericPrecision(Integer numericPrecision) {
            field.columnDefinition.setNumericPrecision(numericPrecision);
            return self();
        }

        public T ddlNumericScale(Integer numericScale) {
            field.columnDefinition.setNumericScale(numericScale);
            return self();
        }

        public T ddlIndex(Boolean index) {
            field.columnDefinition.setIndex(index);
            return self();
        }

        public T ddlIndexName(String indexName) {
            field.columnDefinition.setIndexName(indexName);
            return self();
        }

        public T ddlIndexType(IndexType indexType) {
            field.columnDefinition.setIndexType(indexType);
            return self();
        }

        public T ddlCustomIndexColumn(String customIndexColumn) {
            field.columnDefinition.setCustomIndexColumn(customIndexColumn);
            return self();
        }


        public T ddlDefaultValue(String defaultValue) {
            field.columnDefinition.setDefaultValue(defaultValue);
            return self();
        }

        public T ddlNotNull(Boolean notNull) {
            field.columnDefinition.setNotNull(notNull);
            return self();
        }

        public T ddlComment(String comment) {
            field.columnDefinition.setComment(comment);
            return self();
        }

        public T ddlOldColumnNames(List<String> oldColumnNames) {
            field.columnDefinition.setOldColumnNames(oldColumnNames);
            return self();
        }

        public T ddlAdditionalDDl(String additionalDDl) {
            field.columnDefinition.setAdditionalDDl(additionalDDl);
            return self();
        }

        public T ddlDisableAlterComment(Boolean disableAlterComment) {
            field.columnDefinition.setDisableAlterComment(disableAlterComment);
            return self();
        }

        public T ddlAlterOrDropStrategy(AlterOrDropStrategy alterOrDropStrategy) {
            field.columnDefinition.setAlterOrDropStrategy(alterOrDropStrategy);
            return self();
        }

        @SneakyThrows
        public static Class<?> convertToClass(@NonNull String javaType) {
            if (javaType.equalsIgnoreCase("string")) {
                return String.class;
            } else if (javaType.equalsIgnoreCase("int") || javaType.equalsIgnoreCase("integer")) {
                return Integer.class;
            } else if (javaType.equalsIgnoreCase("long")) {
                return Long.class;
            } else if (javaType.equalsIgnoreCase("boolean") || javaType.equalsIgnoreCase("bool")) {
                return Boolean.class;
            } else if (javaType.equalsIgnoreCase("float")) {
                return Float.class;
            } else if (javaType.equalsIgnoreCase("double")) {
                return Double.class;
            } else if (javaType.equalsIgnoreCase("date")) {
                return LocalDate.class;
            } else if (javaType.equalsIgnoreCase("datetime")) {
                return LocalDateTime.class;
            } else if (javaType.equalsIgnoreCase("map")) {
                return Map.class;
            } else if (javaType.equalsIgnoreCase("list")) {
                return List.class;
            } else if (javaType.equalsIgnoreCase("enum")) {
                return Enum.class;
            } else {
                return Class.forName(javaType);
            }
        }
    }

}
