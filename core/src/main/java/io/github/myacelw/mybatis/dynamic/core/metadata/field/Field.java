package io.github.myacelw.mybatis.dynamic.core.metadata.field;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.ExtProperties;
import io.github.myacelw.mybatis.dynamic.core.service.ModelToTableConverter;
import lombok.NonNull;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 模型字段
 *
 * @author liuwei
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = BasicField.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BasicField.class, name = "BasicField", names = {"Basic", "basic", "BASIC"}),
        @JsonSubTypes.Type(value = GroupField.class, name = "GroupField", names = {"Group", "group", "GROUP"}),
        @JsonSubTypes.Type(value = ToOneField.class, name = "ToOneField", names = {"ToOne", "to_one", "TO_ONE"}),
        @JsonSubTypes.Type(value = ToManyField.class, name = "ToManyField", names = {"ToMany", "to_many", "TO_MANY"}),
})
public interface Field extends ExtProperties, Cloneable {
    /**
     * 通配符，用于查询所有字段
     */
    String ALL_WILDCARD = "*";

    /**
     * 字段对应的数据库列向量长度
     */
    String EXT_PROPERTY_COLUMN_VECTOR_LENGTH = "VECTOR_LENGTH";


    /**
     * 字段名
     */
    String getName();

    /**
     * type为 Basic 时，需要字段填写Java类型，目前支持(参见 ModelToTableConverter.convertColumn方法)：
     * 1、 String、Integer、Float、Double、Boolean、Date、LocalDateTime、LocalDate
     * 2、 Enum： 存储枚举的 name
     * 3、 Map、List、Set ： 将转换为 json 存储
     */
    Class<?> getJavaClass();

    /**
     * 初始化
     */
    void init(Model model, ModelToTableConverter converter);

    /**
     * 检查合法性
     */
    void check();


    Object sampleData();

    Field clone();


    /**
     * 基本字段构建器
     *
     * @param name     字段名
     * @param javaType 字段类型
     * @return 字段构建器
     */
    static BasicField.Builder<?> basicBuilder(String name, Class<?> javaType) {
        return new BasicField.Builder<>(name, javaType);
    }

    static BasicField.Builder<?> basicBuilder(String name, String javaType) {
        return new BasicField.Builder<>(name, javaType);
    }

    static BasicField.BuilderAndMaxLength stringBuilder(String name) {
        return new BasicField.BuilderAndMaxLength(name, String.class);
    }

    static BasicField.Builder<?> integerBuilder(String name) {
        return new BasicField.Builder<>(name, Integer.class);
    }

    static BasicField.Builder<?> longBuilder(String name) {
        return new BasicField.Builder<>(name, Long.class);
    }

    static BasicField.Builder<?> booleanBuilder(String name) {
        return new BasicField.Builder<>(name, Boolean.class);
    }

    static BasicField.BuilderAndPrecisionAndScale floatBuilder(String name) {
        return new BasicField.BuilderAndPrecisionAndScale(name, Float.class);
    }

    static BasicField.BuilderAndPrecisionAndScale doubleBuilder(String name) {
        return new BasicField.BuilderAndPrecisionAndScale(name, Double.class);
    }

    static BasicField.BuilderAndPrecisionAndScale bigDecimalBuilder(String name) {
        return new BasicField.BuilderAndPrecisionAndScale(name, BigDecimal.class);
    }

    static BasicField.Builder<?> dateBuilder(String name) {
        return new BasicField.Builder<>(name, LocalDate.class);
    }

    static BasicField.Builder<?> dateTimeBuilder(String name) {
        return new BasicField.Builder<>(name, LocalDateTime.class);
    }

    static BasicField.Builder<?> timestampBuilder(String name) {
        return new BasicField.Builder<>(name, LocalDateTime.class).jdbcType(JdbcType.TIMESTAMP);
    }

    static BasicField.Builder<?> enumBuilder(String name) {
        return new BasicField.Builder<>(name, Enum.class);
    }

    static BasicField.BuilderAndMaxLength mapBuilder(String name) {
        return new BasicField.BuilderAndMaxLength(name, Map.class);
    }

    static BasicField.BuilderAndMaxLength listBuilder(String name) {
        return new BasicField.BuilderAndMaxLength(name, List.class);
    }

    static BasicField string(@NonNull String name, Integer maxLength) {
        return stringBuilder(name).characterMaximumLength(maxLength).build();
    }

    static BasicField of(@NonNull String name, @NonNull Class<?> javaType) {
        return basicBuilder(name, javaType).build();
    }

    static BasicField of(@NonNull String name, @NonNull String javaType) {
        return basicBuilder(name, javaType).build();
    }

    static BasicField integer(@NonNull String name) {
        return integerBuilder(name).build();
    }

    static BasicField bool(@NonNull String name) {
        return booleanBuilder(name).build();
    }

    static BasicField floatOf(@NonNull String name) {
        return floatBuilder(name).build();
    }

    static BasicField doubleOf(@NonNull String name) {
        return doubleBuilder(name).build();
    }

    static BasicField bigDecimal(@NonNull String name) {
        return bigDecimalBuilder(name).build();
    }

    static BasicField date(@NonNull String name) {
        return dateBuilder(name).build();
    }

    static BasicField dateTime(@NonNull String name) {
        return dateTimeBuilder(name).build();
    }

    static BasicField timestamp(@NonNull String name) {
        return timestampBuilder(name).build();
    }

    static BasicField enumOf(@NonNull String name) {
        return enumBuilder(name).build();
    }

    static BasicField map(@NonNull String name) {
        return mapBuilder(name).build();
    }

    static BasicField list(@NonNull String name) {
        return listBuilder(name).build();
    }

    static ToOneField.Builder manyToOne(@NonNull String name, @NonNull String targetModel, String... joinLocalFields) {
        return new ToOneField.Builder(name, targetModel).joinLocalFields(joinLocalFields).javaClass(Map.class);
    }

    static ToOneField manyToOne(@NonNull String name, @NonNull String targetModel) {
        return manyToOne(name, targetModel, Model.FIELD_ID).build();
    }

    static GroupField.Builder fieldGroupBuilder(@NonNull String name) {
        return new GroupField.Builder(name).javaClass(Map.class);
    }

    static GroupField fieldGroup(@NonNull String name, BasicField... subFields) {
        return fieldGroupBuilder(name).fields(subFields).build();
    }

    static ToManyField.Builder oneToManyBuilder(@NonNull String name, @NonNull String targetModel, @NonNull String joinTargetField) {
        return new ToManyField.Builder(name, targetModel, joinTargetField).javaClass(Map.class);
    }

    static ToManyField oneToMany(@NonNull String name, @NonNull String targetModel, @NonNull String joinTargetField) {
        return oneToManyBuilder(name, targetModel, joinTargetField).build();
    }


}
