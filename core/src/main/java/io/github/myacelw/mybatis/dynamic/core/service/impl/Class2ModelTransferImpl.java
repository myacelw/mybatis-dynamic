package io.github.myacelw.mybatis.dynamic.core.service.impl;

import io.github.myacelw.mybatis.dynamic.core.annotation.*;
import io.github.myacelw.mybatis.dynamic.core.annotation.partition.Partition;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.KeyGeneratorMode;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.*;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.GroupField;
import io.github.myacelw.mybatis.dynamic.core.metadata.partition.HashPartition;
import io.github.myacelw.mybatis.dynamic.core.metadata.partition.KeyPartition;
import io.github.myacelw.mybatis.dynamic.core.metadata.partition.PartitionFactory;
import io.github.myacelw.mybatis.dynamic.core.service.Class2ModelTransfer;
import io.github.myacelw.mybatis.dynamic.core.util.*;
import io.github.myacelw.mybatis.dynamic.core.util.tuple.Tuple3;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 实体类转换为模型实现类。
 * String、int、boolean、float、double、枚举、Map等类型属性映射为Basic类型字段，
 * List类型属性，泛型类如果是另外一个非java类型非枚举类型的Class，则映射为ToMany类型字段，否则映射为Basic类型字段。
 * 实体类属性上可以增加 @BasicField 明确指定 字段类型、字符串最大长度、关联模型、是否需要创建索引、是否必填等。
 *
 * @author liuwei
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Class2ModelTransferImpl implements Class2ModelTransfer {

    private static final Map<Class<?>, Model> MODEL_CACHE = new ConcurrentHashMap<>();

    /**
     * 注释注解类型，例如填写为 io.swagger.v3.oas.annotations.media.Schema
     */
    private Class<? extends Annotation> commentAnnotationClass;

    /**
     * 注释注解字段名称，例如填写为 title
     */
    private String commentAnnotationFieldName;

    @Override
    public Model getModelForClass(@NonNull Class<?> entityClass) {
        Model model = MODEL_CACHE.computeIfAbsent(entityClass, this::doGetModelForClass);
        return model.clone();
    }

    @SneakyThrows
    protected void setComment(AnnotatedElement annotatedElement, Consumer<String> commentConsumer) {
        if (commentAnnotationClass != null) {
            Annotation annotation = AnnotationUtil.getAnnotation(annotatedElement, commentAnnotationClass);
            if (annotation != null) {
                //得到 commentAnnotationFieldName 取值
                String methodName = commentAnnotationFieldName == null ? "value" : commentAnnotationFieldName;
                Method method = commentAnnotationClass.getMethod(methodName);
                Assert.notNull(method, "@" + commentAnnotationClass + " method not found: " + methodName);
                Object value = method.invoke(annotation);
                if (value != null && !Objects.equals("", value)) {
                    commentConsumer.accept(value.toString());
                }
            }
        }
    }

    protected Model doGetModelForClass(@NonNull Class<?> entityClass) {
        Model model = new Model();
        model.setName(getModelName(entityClass));
        model.setJavaType(entityClass);

        ExtProperty[] extProperties = AnnotationUtil.getAnnotations(entityClass, ExtProperty.class);
        for (ExtProperty extProperty : extProperties) {
            model.putExtProperty(extProperty.key(), extProperty.value());
        }
        if (!model.containsExtPropertyKey(Model.EXT_PROPERTY_MODULE_GROUP)) {
            model.putExtProperty(Model.EXT_PROPERTY_MODULE_GROUP, entityClass.getPackage().getName());
        }

        io.github.myacelw.mybatis.dynamic.core.annotation.Model anno = entityClass.getDeclaredAnnotation(io.github.myacelw.mybatis.dynamic.core.annotation.Model.class);
        boolean logicDelete = false;
        boolean autoField = true;
        if (anno != null) {
            autoField = anno.autoField();
            if (StringUtil.hasText(anno.comment())) {
                model.getTableDefine().setComment(anno.comment());
            }
            if (StringUtil.hasText(anno.tableName())) {
                model.setTableName(anno.tableName());
            }
            model.getTableDefine().setDisableTableCreateAndAlter(anno.disableTableCreateAndAlter());
            model.getTableDefine().setPartition(getPartition(anno.partition()));
            logicDelete = anno.logicDelete();
        }

        if (model.getTableDefine().getPartition() == null) {
            setComment(entityClass, model.getTableDefine()::setComment);
        }

        Tuple3<String, Map<String, Class<?>>, List<Field>> tuple3 = getSubTypesFields(entityClass, false, logicDelete, autoField);
        if (tuple3 != null) {
            model.putExtProperty(Model.EXT_PROPERTY_SUB_TYPE_FIELD_NAME, tuple3.v1);
            model.putExtProperty(Model.EXT_PROPERTY_SUB_TYPE_MAP, tuple3.v2);
            model.setFields(tuple3.v3);
        } else {
            model.setFields(getFields(entityClass, false, logicDelete, entityClass.getSimpleName(), autoField));
        }
        handleLogicDeleteField(model, logicDelete, false);

        //设置ID字段
        List<IdFieldInfo> idFields = getIdFieldInfos(entityClass);

        if (!ObjectUtil.isEmpty(idFields)) {
            String[] ids = idFields.stream().map(IdFieldInfo::getName).toArray(String[]::new);
            model.setPrimaryKeyFields(ids);
            model.setKeyGeneratorMode(idFields.get(0).getKeyGeneratorMode());
        }

        model.getFields().forEach(field -> {
            if (field instanceof ToManyField) {
                fixAndCheckToMany(model, (ToManyField) field);
            } else if (field instanceof ToOneField) {
                fixAndCheckToOne(model, (ToOneField) field);
            }
        });
        // id字段排到最前面
        sortFields(model);
        return model;
    }

    protected String getModelName(Class<?> entityClass) {
        io.github.myacelw.mybatis.dynamic.core.annotation.Model anno = entityClass.getDeclaredAnnotation(io.github.myacelw.mybatis.dynamic.core.annotation.Model.class);

        if (anno != null) {
            if (StringUtil.hasText(anno.name())) {
                return anno.name();
            }
        }
        return entityClass.getSimpleName();
    }


    @SneakyThrows
    protected io.github.myacelw.mybatis.dynamic.core.metadata.partition.Partition getPartition(Partition anno) {
        io.github.myacelw.mybatis.dynamic.core.metadata.partition.Partition partition = getPartition(
                anno.key(),
                anno.hash(),
                anno.field(),
                anno.factory().factory(),
                anno.factory().params(),
                true
        );
        if (partition != null) {
            partition.setSubPartition(getPartition(
                    anno.level2().key(),
                    anno.level2().hash(),
                    anno.level2().field(),
                    anno.level2().factory().factory(),
                    anno.level2().factory().params(),
                    false
            ));
        }
        return partition;
    }

    @SneakyThrows
    protected io.github.myacelw.mybatis.dynamic.core.metadata.partition.Partition getPartition(
            int keyNum,
            int hashNum,
            String field,
            Class<? extends PartitionFactory> partitionFactoryClass,
            String[] params,
            boolean mainPartition
    ) {
        io.github.myacelw.mybatis.dynamic.core.metadata.partition.Partition partition = null;
        if (keyNum > 0) {
            partition = new KeyPartition(StringUtil.hasText(field) ? field : null, keyNum);
        } else if (hashNum > 0 && StringUtil.hasText(field)) {
            partition = new HashPartition(field, hashNum);
        } else if (partitionFactoryClass != PartitionFactory.None.class) {
            PartitionFactory partitionFactory = partitionFactoryClass.newInstance();
            partition = partitionFactory.create(field, params, mainPartition);
        }
        return partition;
    }


    protected void handleLogicDeleteField(Model model, boolean logicDelete, boolean physicalDelete) {
        Field field = model.findField(Model.FIELD_DELETE_FLAG);
        if (physicalDelete) {
            if (field != null) {
                model.getFields().remove(field);
            }
        } else if (logicDelete && field == null) {
            model.getFields().add(Model.createLogicDeleteField());
        }
    }

    protected Tuple3<String, Map<String, Class<?>>, List<Field>> getSubTypesFields(Class<?> clazz, boolean innerField, boolean logicDelete, boolean autoField) {
        SubTypes anno = AnnotationUtil.getAnnotation(clazz, SubTypes.class);
        if (anno == null || ObjectUtil.isEmpty(anno.subTypes())) {
            return null;
        }

        Map<String, Class<?>> subTypeMap = new HashMap<>();
        Map<String, Field> fields = new LinkedHashMap<>();

        // 子类类型字段
        Field subTypeField = Field.string(anno.subTypeFieldName(), 100);
        fields.put(subTypeField.getName(), subTypeField);

        for (SubTypes.SubType subType : anno.subTypes()) {
            Class<?> type = subType.value();
            String name = StringUtil.hasText(subType.name()) ? subType.name() : type.getSimpleName();
            subTypeMap.put(name, type);

            List<Field> list = getFields(type, innerField, logicDelete, type.getSimpleName(), autoField);
            list.stream()
                    .filter(t -> !fields.containsKey(t.getName()))
                    .forEach(t -> fields.put(t.getName(), t));
        }
        return new Tuple3<>(anno.subTypeFieldName(), subTypeMap, new ArrayList<>(fields.values()));
    }

    @Value
    public static class IdFieldInfo {
        String name;
        Class<?> propertyType;
        KeyGeneratorMode keyGeneratorMode;
        int order;
    }

    @SneakyThrows
    public static List<IdFieldInfo> getIdFieldInfos(Class<?> entityClass) {
        List<IdFieldInfo> idFields = new ArrayList<>();

        PropertyDescriptor[] pds = BeanUtil.getPropertyDescriptors(entityClass);
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null && pd.getReadMethod() != null) {
                AnnotatedElement classField = BeanUtil.getField(entityClass, pd.getName());
                if (classField == null) {
                    classField = pd.getWriteMethod().getParameters()[0];
                }
                IdField id = classField.getAnnotation(IdField.class);
                if (id != null) {
                    idFields.add(new IdFieldInfo(pd.getName(), pd.getPropertyType(), id.keyGeneratorMode(), id.order()));
                }
            }
        }
        return idFields.stream().sorted(Comparator.comparing(IdFieldInfo::getOrder)).collect(Collectors.toList());
    }

    @SneakyThrows
    protected List<Field> getFields(Class<?> entityClass, boolean innerField, boolean logicDelete, String modelDefaultName, boolean autoField) {
        //发现的字段按照类继承的层级进行保存
        List<List<Field>> superFields = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            superFields.add(new ArrayList<>());
        }

        PropertyDescriptor[] pds = BeanUtil.getPropertyDescriptors(entityClass);

        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null && pd.getReadMethod() != null) {
                Class<?> declaringClass;
                java.lang.reflect.Field declaringField = BeanUtil.getField(entityClass, pd.getName());
                if (declaringField != null) {
                    declaringClass = declaringField.getDeclaringClass();
                } else {
                    declaringClass = pd.getWriteMethod().getDeclaringClass();
                }

                //找到字段定义的类
                Class<?> superClass = entityClass;
                int index = 0;
                while (superClass != null && index < superFields.size() - 1 && declaringClass != superClass) {
                    superClass = superClass.getSuperclass();
                    index++;
                }

                AnnotatedElement classField = declaringField;
                if (classField == null) {
                    classField = pd.getWriteMethod().getParameters()[0];
                }
                Field field = getField(entityClass, pd.getName(), pd.getPropertyType(), classField, innerField, logicDelete, modelDefaultName, autoField);
                if (field != null) {
                    if (field instanceof BasicField) {
                        BasicField basicField = (BasicField) field;
                        if (basicField.getColumnDefinition().getComment() == null) {
                            setComment(classField, t -> basicField.getColumnDefinition().setComment(t));
                        }
                    }

                    ExtProperty[] extProperties = AnnotationUtil.getAnnotations(classField, ExtProperty.class);
                    for (ExtProperty extProperty : extProperties) {
                        if (field.getExtProperties() == null || !field.getExtProperties().containsKey(extProperty.key())) {
                            field.putExtProperty(extProperty.key(), extProperty.value());
                        }
                    }
                    superFields.get(index).add(field);
                }
            }
        }

        List<Field> fields = new ArrayList<>();
        //按照继承的层级进行合并字段，也就是保留了层级的顺序
        for (List<Field> superField : superFields) {
            fields.addAll(superField);
        }

        return fields;
    }

    protected Field getField(Class<?> entityClass, String name, Class<?> javaType, AnnotatedElement classField, boolean innerField, boolean logicDelete, String modelDefaultName, boolean autoField) {
        Class<?> parameterizedClass = null;
        if (List.class.isAssignableFrom(javaType)) {
            parameterizedClass = getParameterizedClass(entityClass, classField);
        }

        //===
        IgnoreField ignore = classField.getAnnotation(IgnoreField.class);
        if (ignore != null) {
            return null;
        }

        IdField id = classField.getAnnotation(IdField.class);
        if (id != null) {
            return getIdField(name, javaType, parameterizedClass, id);
        }

        io.github.myacelw.mybatis.dynamic.core.annotation.BasicField basic = classField.getAnnotation(io.github.myacelw.mybatis.dynamic.core.annotation.BasicField.class);
        if (basic != null) {
            return getBasicField(name, javaType, parameterizedClass, basic);
        }

        ToOne toOne = classField.getAnnotation(ToOne.class);
        if (toOne != null) {
            return getToOneField(name, javaType, toOne);
        }

        io.github.myacelw.mybatis.dynamic.core.annotation.GroupField fieldGroup = classField.getAnnotation(io.github.myacelw.mybatis.dynamic.core.annotation.GroupField.class);
        if (fieldGroup != null) {
            return getFieldGroupField(name, javaType, logicDelete, fieldGroup, modelDefaultName, autoField);
        }

        ToMany toMany = classField.getAnnotation(ToMany.class);
        if (toMany != null) {
            return getToManyField(name, javaType, parameterizedClass, toMany, modelDefaultName);
        }

        if (!autoField) {
            return null;
        }

        //集合类型，并且集合参数类型不为基本类型，如果对应实体是也是模型 则映射为ToManyField， 否则映射为BasicField
        if (!innerField && List.class.isAssignableFrom(javaType) && parameterizedClass != null && !parameterizedClass.isEnum() && !parameterizedClass.isArray() && !parameterizedClass.getName().startsWith("java.")) {
            return getFieldForListType(name, javaType, logicDelete, parameterizedClass);
        }

        //非基本类型
        if (!List.class.isAssignableFrom(javaType) && !DataUtil.isBasicType(javaType)) {
            return getFieldForNoBasicType(name, javaType, logicDelete, modelDefaultName, autoField);
        }

        BasicField field = new BasicField();
        field.setName(name);
        field.setJavaClass(javaType);
        field.setJavaParameterClass(parameterizedClass);
        return field;
    }

    protected AbstractField getFieldForNoBasicType(String name, Class<?> javaType, boolean logicDelete, String modelDefaultName, boolean autoField) {
        Optional<io.github.myacelw.mybatis.dynamic.core.annotation.Model> ma = getModelAnnotation(javaType);
        // 如果字段类型对象存在@Model 注解，则认为是 ToOneField
        if (ma.isPresent()) {
            ToOneField field = new ToOneField();
            field.setName(name);
            field.setJavaClass(javaType);
            field.setTargetModel(ma.map(t -> t.name()).filter(StringUtil::hasText).orElse(javaType.getSimpleName()));
            return field;
        } else {
            // 否则映射为 FieldGroup
            BasicField field = new BasicField();
            field.setName(name);
            field.setJavaClass(javaType);
            return field;
        }
    }

    protected AbstractField getFieldForListType(String name, Class<?> javaType, boolean logicDelete, Class<?> parameterizedClass) {
        Optional<io.github.myacelw.mybatis.dynamic.core.annotation.Model> ma = getModelAnnotation(javaType);

        if (ma.isPresent()) {
            ToManyField field = new ToManyField();
            field.setName(name);
            field.setTargetModel(ma.map(t -> t.name()).filter(StringUtil::hasText).orElse(parameterizedClass.getSimpleName()));

            field.setJavaClass(parameterizedClass);
            return field;
        } else {
            // 映射为 BasicField
            return Field.listBuilder(name).javaParameterClass(parameterizedClass).build();
        }
    }

    protected BasicField getIdField(String name, Class<?> javaType, Class<?> parameterizedClass, IdField basic) {
        BasicField field = new BasicField();
        field.setName(name);
        field.setJavaClass(javaType);
        field.setJavaParameterClass(parameterizedClass);
        if (basic.jdbcType() != JdbcType.UNDEFINED) {
            field.setJdbcType(basic.jdbcType());
        }
        if (basic.typeHandler() != TypeHandler.class) {
            field.setTypeHandlerClass(basic.typeHandler());
        }

        if (StringUtil.hasText(basic.ddlColumnType())) {
            field.getColumnDefinition().setColumnType(basic.ddlColumnType());
        }
        field.getColumnDefinition().setAlterOrDropStrategy(basic.ddlColumnAlterStrategy());
        if (basic.ddlCharacterMaximumLength() != Integer.MIN_VALUE) {
            field.getColumnDefinition().setCharacterMaximumLength(basic.ddlCharacterMaximumLength());
        }
        if (StringUtil.hasText(basic.columnName())) {
            field.setColumnName(basic.columnName());
        }
        if (StringUtil.hasText(basic.ddlComment())) {
            field.getColumnDefinition().setComment(basic.ddlComment());
        }
        return field;
    }


    protected BasicField getBasicField(String name, Class<?> javaType, Class<?> parameterizedClass, io.github.myacelw.mybatis.dynamic.core.annotation.BasicField basic) {
        BasicField field = new BasicField();
        field.setName(name);
        field.setJavaClass(javaType);
        field.setJavaParameterClass(parameterizedClass);
        field.setFillerName(basic.fillerName().isEmpty() ? null : basic.fillerName());
        field.setSelect(basic.select());
        if (StringUtil.hasText(basic.columnName())) {
            field.setColumnName(basic.columnName());
        }
        if (basic.typeHandler() != TypeHandler.class) {
            field.setTypeHandlerClass(basic.typeHandler());
        }
        if (basic.jdbcType() != JdbcType.UNDEFINED) {
            field.setJdbcType(basic.jdbcType());
        }
        if (StringUtil.hasText(basic.ddlColumnType())) {
            field.getColumnDefinition().setColumnType(basic.ddlColumnType());
        }
        if (StringUtil.hasText(basic.ddlDefaultValue())) {
            field.getColumnDefinition().setDefaultValue(basic.ddlDefaultValue());
        }
        field.getColumnDefinition().setNotNull(basic.ddlNotNull());
        field.getColumnDefinition().setIndex(basic.ddlIndex());
        if (StringUtil.hasText(basic.ddlIndexName())) {
            field.getColumnDefinition().setIndexName(basic.ddlIndexName());
        }
        field.getColumnDefinition().setIndexType(basic.ddlIndexType());
        field.getColumnDefinition().setAlterOrDropStrategy(basic.ddlColumnAlterStrategy());
        if (basic.ddlNumericPrecision() != Integer.MIN_VALUE) {
            field.getColumnDefinition().setNumericPrecision(basic.ddlNumericPrecision());
        }
        if (basic.ddlNumericScale() != Integer.MIN_VALUE) {
            field.getColumnDefinition().setNumericScale(basic.ddlNumericScale());
        }
        if (basic.ddlCharacterMaximumLength() != Integer.MIN_VALUE) {
            field.getColumnDefinition().setCharacterMaximumLength(basic.ddlCharacterMaximumLength());
        }
        if (StringUtil.hasText(basic.ddlComment())) {
            field.getColumnDefinition().setComment(basic.ddlComment());
        }
        return field;
    }

    protected ToOneField getToOneField(String name, Class<?> javaType, ToOne toOne) {
        ToOneField field = new ToOneField();
        field.setName(name);
        field.setJavaClass(javaType);

        if (StringUtil.hasText(toOne.targetModel())) {
            field.setTargetModel(toOne.targetModel());
        } else {
            field.setTargetModel(getModelName(javaType));
        }
        if (!ObjectUtil.isEmpty(toOne.joinLocalFields())) {
            field.setJoinLocalFields(toOne.joinLocalFields());
        } else {
            List<IdFieldInfo> idFields = getIdFieldInfos(javaType);
            if (!ObjectUtil.isEmpty(idFields)) {
                field.setJoinLocalFields(idFields.stream().map(t -> name + StringUtil.upperFirst(t.getName())).toArray(String[]::new));
            }
        }
        return field;
    }

    protected GroupField getFieldGroupField(String name, Class<?> javaType, boolean logicDelete, io.github.myacelw.mybatis.dynamic.core.annotation.GroupField fieldGroup, String modelDefaultName, boolean autoField) {
        GroupField field = new GroupField();
        field.setName(name);
        field.setJavaClass(javaType);
        field.setSelect(fieldGroup.select());
        field.setFields(getFields(javaType, true, logicDelete, modelDefaultName, autoField).stream().filter(t -> t instanceof BasicField).map(t -> (BasicField) t).collect(Collectors.toList()));

        String constFieldName = io.github.myacelw.mybatis.dynamic.core.annotation.GroupField.FIELD_NAME;

        for (BasicField fieldField : field.getFields()) {
            if (!fieldGroup.columnPrefix().isEmpty()) {
                String columnName = StringUtil.hasText(fieldField.getColumnName()) ? fieldField.getColumnName() : fieldField.getName();
                String columnPrefix = constFieldName.equals(fieldGroup.columnPrefix()) ? name + "_" : fieldGroup.columnPrefix();
                fieldField.setColumnName(columnPrefix + columnName);
            }
            if (!fieldGroup.ddlIndexNamePrefix().isEmpty()) {
                String indexName = StringUtil.hasText(fieldField.getColumnDefinition().getIndexName()) ? fieldField.getColumnDefinition().getIndexName() : fieldField.getName();
                String indexPrefix = constFieldName.equals(fieldGroup.ddlIndexNamePrefix()) ? name + "_" : fieldGroup.ddlIndexNamePrefix();
                fieldField.getColumnDefinition().setIndexName(indexPrefix + indexName);
            }
            if (!fieldGroup.ddLCommentPrefix().isEmpty()) {
                String comment = StringUtil.hasText(fieldField.getColumnDefinition().getComment()) ? fieldField.getColumnDefinition().getComment() : fieldField.getName();
                String commentPrefix = constFieldName.equals(fieldGroup.ddLCommentPrefix()) ? name : fieldGroup.ddLCommentPrefix();
                fieldField.setComment(commentPrefix + comment);
            }
            if (!fieldGroup.ddlIndexEnabled()) {
                fieldField.getColumnDefinition().setIndex(false);
            }
            if (!fieldGroup.ddlRequiredEnabled()) {
                fieldField.getColumnDefinition().setNotNull(false);
            }
        }

        return field;
    }

    protected ToManyField getToManyField(String name, Class<?> javaType, Class<?> parameterizedClass, ToMany toMany, String modelDefaultName) {
        ToManyField field = new ToManyField();
        field.setName(name);

        if (StringUtil.hasText(toMany.targetModel())) {
            field.setTargetModel(toMany.targetModel());
        } else if (parameterizedClass != null) {
            field.setTargetModel(getModelName(parameterizedClass));
        } else {
            throw new IllegalArgumentException("Model '" + modelDefaultName + "' ToMany field '" + name + "' targetModel attribute cannot be null");
        }
        field.setJavaClass(parameterizedClass == null ? Map.class : parameterizedClass);

        if (!ObjectUtil.isEmpty(toMany.joinTargetFields())) {
            field.setJoinTargetFields(toMany.joinTargetFields());
        }
        return field;
    }

    protected void fixAndCheckToMany(Model model, ToManyField field) {
        if (ObjectUtil.isEmpty(field.getJoinTargetFields()) && !ObjectUtil.isEmpty(model.getPrimaryKeyFields())) {
            String relFieldName = StringUtil.lowerFirst(model.getName());
            field.setJoinTargetFields(Arrays.stream(model.getPrimaryKeyFields()).map(t -> relFieldName + StringUtil.upperFirst(t)).toArray(String[]::new));
        }
        Assert.isTrue(!ObjectUtil.isEmpty(field.getTargetModel()), "Model '" + model.getName() + "' ToMany field '" + field.getName() + "' targetModel attribute cannot be null");
        Assert.isTrue(!ObjectUtil.isEmpty(field.getJoinTargetFields()), "Model '" + model.getName() + "' ToMany field '" + field.getName() + "' joinTargetFields attribute cannot be null");
    }

    protected void fixAndCheckToOne(Model model, ToOneField field) {
        Assert.isTrue(!ObjectUtil.isEmpty(field.getJoinLocalFields()), "Model '" + model.getName() + "' ToOne field '" + field.getName() + "' joinLocalFields attribute cannot be null");
        for (String localField : field.getJoinLocalFields()) {
            Assert.notNull(model.findField(localField), "Model '" + model.getName() + "' ToOne field '" + field.getName() + "' joinLocalField '" + localField + "' not found in model. Please explicitly define this field with @BasicField.");
        }
    }

    /**
     * 字段排序，id字段排到最前面，deleteFlag字段排到最后面
     */
    protected void sortFields(Model model) {
        // id字段排到最前面
        // id字段排到最前面，deleteFlag字段排到最后面
        model.getFields().sort(Comparator.comparing(f -> {
            if (f.getName().equals(Model.FIELD_ID)) {
                return -1;
            } else if (f.getName().equals(Model.FIELD_DELETE_FLAG)) {
                return 1;
            } else {
                return 0;
            }
        }));
    }


    protected Optional<io.github.myacelw.mybatis.dynamic.core.annotation.Model> getModelAnnotation(Class<?> javaType) {
        io.github.myacelw.mybatis.dynamic.core.annotation.Model anno = AnnotationUtil.getAnnotation(javaType, io.github.myacelw.mybatis.dynamic.core.annotation.Model.class);
        return Optional.ofNullable(anno);
    }

    @SneakyThrows
    protected Class<?> getParameterizedClass(Class<?> entityClass, AnnotatedElement field) {
        Type genericType;
        if (field instanceof java.lang.reflect.Field) {
            genericType = ((java.lang.reflect.Field) field).getGenericType();
        } else {
            genericType = ((Parameter) field).getParameterizedType();
        }

        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Type type0 = actualTypeArguments[0];
            if (type0 instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) type0).getRawType();
            } else if (type0 instanceof TypeVariable) {
                TypeVariable<?> typeVariable = (TypeVariable<?>) type0;
                Map<String, Class<?>> genericTypeMap = getGenericTypeMap(entityClass);
                return genericTypeMap.get(typeVariable.getName());
            } else {
                return (Class<?>) type0;
            }
        } else {
            return null;
        }
    }

    /**
     * 获取类及其父类中的泛型参数类型映射
     *
     * @param clazz 类
     * @return 泛型参数名称与具体类型的映射
     */
    protected Map<String, Class<?>> getGenericTypeMap(Class<?> clazz) {
        Map<String, Class<?>> genericTypeMap = new HashMap<>();

        // 解析类的父类中的泛型
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;

            // 获取实际类型和声明类型
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            TypeVariable<?>[] typeParameters = ((Class<?>) parameterizedType.getRawType()).getTypeParameters();

            // 映射泛型参数名和对应的实际类型
            for (int i = 0; i < typeParameters.length; i++) {
                if (actualTypeArguments[i] instanceof Class<?>) {
                    genericTypeMap.put(typeParameters[i].getName(), (Class<?>) actualTypeArguments[i]);
                }
            }
        }

        return genericTypeMap;
    }

}
