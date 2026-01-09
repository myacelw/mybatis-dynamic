package io.github.myacelw.mybatis.dynamic.core.service;

import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.GroupField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.RefModel;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.lambda.LambdaUtil;
import io.github.myacelw.mybatis.dynamic.core.util.lambda.SFunction;

import java.util.*;

/**
 * select字段构建器
 *
 * @author liuwei
 */
public class SelectBuilder {

    protected final DataManager<?> dataManager;

    private final Set<String> selectFields = new HashSet<>();

    public SelectBuilder(DataManager<?> dataManager) {
        this.dataManager = dataManager;
    }

    public List<String> build() {
        return new ArrayList<>(selectFields);
    }

    /**
     * 插入多个字段
     */
    public SelectBuilder select(String... fields) {
        selectFields.addAll(Arrays.asList(fields));
        return this;
    }

    /**
     * 排除多个字段；
     * 注意可以先使用includeAll插入全部字段，然后再调用exclude排除部分字段。
     */
    public SelectBuilder exclude(String... fields) {
        for (String field : fields) {
            selectFields.remove(field);
        }
        return this;
    }

    /**
     * 插入多个字段
     */
    @SafeVarargs
    public final <T> SelectBuilder select(SFunction<T, ?>... getters) {
        selectFields.addAll(LambdaUtil.names(getters));
        return this;
    }

    /**
     * 排除多个字段；
     * 注意可以先使用includeAll插入全部字段，然后再调用exclude排除部分字段。
     */
    @SafeVarargs
    public final <T> SelectBuilder exclude(SFunction<T, ?>... getters) {
        selectFields.removeAll(LambdaUtil.names(getters));
        return this;
    }

    /**
     * 插入主表的全部 Basic、Group 类型字段
     */
    public SelectBuilder selectAll() {
        dataManager.getModelContext().getPermissionFields()
                .forEach(field -> {
                    if (field instanceof BasicField) {
                        selectFields.add(field.getName());
                    } else if (field instanceof GroupField) {
                        ((GroupField) field).getFields().forEach(f -> selectFields.add(field.getName() + "." + f.getName()));
                    }
                });
        return this;
    }

    /**
     * 插入主表的全部 Basic、Group 类型字段
     */
    public SelectBuilder selectDefault() {
        dataManager.getModelContext().getPermissionFields()
                .forEach(field -> {
                    if (field instanceof BasicField) {
                        if (((BasicField) field).getSelect() == Boolean.TRUE) {
                            selectFields.add(field.getName());
                        }
                    } else if (field instanceof GroupField) {
                        if (((GroupField) field).getSelect() == Boolean.TRUE) {
                            ((GroupField) field).getFields().stream()
                                    .filter(f -> f.getSelect() == Boolean.TRUE)
                                    .forEach(f -> selectFields.add(field.getName() + "." + f.getName()));
                        }
                    }
                });
        return this;
    }

    /**
     * 插入字段路径对应的表的全部字段。
     *
     * @param toOneOrToManyOrFieldGroup 字段路径，应指向 ToOne、ToMany、 FieldGroup 类型字段。
     */
    public SelectBuilder selectJoinAll(String toOneOrToManyOrFieldGroup) {
        return selectJoinAll(toOneOrToManyOrFieldGroup.split("\\."));
    }

    public SelectBuilder selectJoinAll(String... toOneOrToManyOrFieldGroupFieldPath) {
        return selectJoinAll(Arrays.asList(toOneOrToManyOrFieldGroupFieldPath));
    }

    protected SelectBuilder selectJoinAll(List<String> toOneOrToManyOrFieldGroupFieldPath) {
        String fieldPathString = String.join(".", toOneOrToManyOrFieldGroupFieldPath);
        Map<String, Field> fields = getFieldsFromPath(toOneOrToManyOrFieldGroupFieldPath);
        fields.forEach((k, v) -> {
            if (v instanceof BasicField) {
                selectFields.add(fieldPathString + "." + k);
            } else if (v instanceof GroupField) {
                ((GroupField) v).getFields().forEach(f -> selectFields.add(fieldPathString + "." + k + "." + f.getName()));
            }
        });
        return this;
    }

    /**
     * 插入字段路径对应的表的默认查询字段。
     *
     * @param toOneOrToManyOrFieldGroup 字段路径，应指向 ToOne、ToMany、 FieldGroup 类型字段。
     */
    public SelectBuilder selectJoinDefault(String toOneOrToManyOrFieldGroup) {
        return selectJoinDefault(toOneOrToManyOrFieldGroup.split("\\."));
    }

    /**
     * 插入字段路径对应的表的默认查询字段。
     *
     * @param toOneOrToManyOrFieldGroupFieldPath 字段路径，应指向 ToOne、ToMany、 FieldGroup 类型字段。
     */
    public SelectBuilder selectJoinDefault(String... toOneOrToManyOrFieldGroupFieldPath) {
        return selectJoinDefault(Arrays.asList(toOneOrToManyOrFieldGroupFieldPath));
    }

    protected SelectBuilder selectJoinDefault(List<String> toOneOrToManyOrFieldGroupFieldPath) {
        String fieldPathString = String.join(".", toOneOrToManyOrFieldGroupFieldPath);
        Map<String, Field> fields = getFieldsFromPath(toOneOrToManyOrFieldGroupFieldPath);
        fields.forEach((k, field) -> {
            if (field instanceof BasicField) {
                if (((BasicField) field).getSelect() == Boolean.TRUE) {
                    selectFields.add(field.getName());
                }
                selectFields.add(fieldPathString + "." + k);
            } else if (field instanceof GroupField) {
                ((GroupField) field).getFields().stream()
                        .filter(f -> f.getSelect() == Boolean.TRUE)
                        .forEach(f -> selectFields.add(fieldPathString + "." + k + "." + f.getName()));
            }
        });
        return this;
    }

    private Map<String, Field> getFieldsFromPath(List<String> fieldPath) {
        Map<String, Field> fields = dataManager.getModelContext().getFieldRights();
        for (String s : fieldPath) {
            Field field = fields.get(s);
            Assert.notNull(field, "没有找到模型 " + dataManager.getModel().getName() + " 的字段 " + s + "。");

            if (field instanceof RefModel) {
                fields = dataManager.getModelContext().getDataManagerGetter().getModelContext(((RefModel) field).getTargetModel()).getFieldRights();
            } else {
                throw new IllegalArgumentException("模型 " + dataManager.getModel().getName() + " 字段 " + s + " 类型为" + field.getClass().getSimpleName() + "，无下级表。");
            }
        }
        return fields;
    }

    /**
     * 插入字段路径对应的表的指定类型
     *
     * @param toOneOrToManyOrFieldGroupFieldPath 字段路径，指向一个 ToOne、ToMany、 FieldGroup 类型字段；
     * @param subFields                          需要加入查询的字段链对应路径表的子字段；
     */
    public SelectBuilder selectJoin(String toOneOrToManyOrFieldGroupFieldPath, String... subFields) {
        return selectJoin(toOneOrToManyOrFieldGroupFieldPath, Arrays.asList(subFields));
    }

    /**
     * 通过字段链，来配置一个字段路径，字段路径应当指向一个 ToOne、ToMany、 FieldGroup 类型字段。
     * 排除该字段路径对应的表的指定字段。
     *
     * @param toOneOrToManyOrFieldGroupFieldPath 字段路径，指向一个 ToOne、ToMany、 FieldGroup 类型字段；
     * @param subFields                          需要排除查询的字段链对应路径表的子字段；
     */
    public SelectBuilder excludeJoin(String toOneOrToManyOrFieldGroupFieldPath, String... subFields) {
        return excludeJoin(toOneOrToManyOrFieldGroupFieldPath, Arrays.asList(subFields));
    }

    /**
     * 通过字段链，来配置一个字段路径，字段路径应当指向一个 ToOne、ToMany、 FieldGroup 类型字段。
     * 插入该字段路径对应的表的指定字段。
     *
     * @param toOneOrToManyOrFieldGroupFieldPath 字段路径，指向一个 ToOne、ToMany、 FieldGroup 类型字段；
     * @param subFields                          需要加入查询的字段链对应路径表的子字段；
     */
    public SelectBuilder selectJoin(String toOneOrToManyOrFieldGroupFieldPath, List<String> subFields) {
        for (String sub : subFields) {
            selectFields.add(toOneOrToManyOrFieldGroupFieldPath + "." + sub);
        }
        return this;
    }

    /**
     * 通过字段链，来配置一个字段路径，字段路径应当指向一个 ToOne、ToMany、 FieldGroup 类型字段。
     * 排除该字段路径对应的表的指定字段。
     *
     * @param toOneOrToManyOrFieldGroupFieldPath 字段路径，指向一个 ToOne、ToMany、 FieldGroup 类型字段；
     * @param subFields                          需要排除查询的字段链对应路径表的子字段；
     */
    public SelectBuilder excludeJoin(String toOneOrToManyOrFieldGroupFieldPath, List<String> subFields) {
        for (String sub : subFields) {
            selectFields.remove(toOneOrToManyOrFieldGroupFieldPath + "." + sub);
        }
        return this;
    }

}
