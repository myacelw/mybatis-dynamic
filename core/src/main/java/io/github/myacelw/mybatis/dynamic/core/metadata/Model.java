package io.github.myacelw.mybatis.dynamic.core.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.myacelw.mybatis.dynamic.core.annotation.SubTypes;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.exception.model.ModelException;
import io.github.myacelw.mybatis.dynamic.core.exception.model.RecursiveFieldException;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.KeyGeneratorMode;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.*;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.ExtProperties;
import io.github.myacelw.mybatis.dynamic.core.service.ModelToTableConverter;
import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractCreatorFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractModifierFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.CreateTimeFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.UpdateTimeFiller;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模型定义
 *
 * @author liuwei
 */
@Data
public class Model implements ExtProperties, Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    //基本字段
    public static final String FIELD_ID = "id";
    public static final String FIELD_DELETE_FLAG = "deleteFlag";

    //常用审计字段
    public static final String FIELD_CREATOR = "creator";
    public static final String FIELD_MODIFIER = "modifier";
    public static final String FIELD_CREATE_TIME = "createTime";
    public static final String FIELD_UPDATE_TIME = "updateTime";

    /**
     * 子类型信息存储字段名
     *
     * @see SubTypes 。
     */
    public static final String EXT_PROPERTY_SUB_TYPE_FIELD_NAME = "SUB_TYPE_FIELD_NAME";
    /**
     * 子类型信息Map，key为子类型名称，value为子类型Class
     *
     * @see SubTypes 。
     */
    public static final String EXT_PROPERTY_SUB_TYPE_MAP = "SUB_TYPE_MAP";

    /**
     * 扩展信息，模型所属模块分组
     */
    public static final String EXT_PROPERTY_MODULE_GROUP = "MODULE_GROUP";

    /**
     * 模型名称
     */
    private String name;

    /**
     * 主键字段名列表
     */
    private String[] primaryKeyFields;

    /**
     * 字段列表
     */
    private List<Field> fields = new ArrayList<>();

    /**
     * 模型对应表名，空时自动生成
     */
    private String tableName;

    /**
     * 表所在数据库名，可空
     */
    private String schema;

    /**
     * 表定义
     */
    private TableDefine tableDefine = new TableDefine();


    /**
     * 模型对应的实体Java类，可空。
     */
    private Class<?> javaType;

    /**
     * 主键生成策略
     */
    private KeyGeneratorMode keyGeneratorMode = KeyGeneratorMode.DEFAULT;

    /**
     * 任意扩展属性
     */
    private Map<String, Object> extProperties;

    public Model addField(Field field) {
        fields.add(field);
        return this;
    }

    public String getSchemaAndTableName() {
        return StringUtil.hasText(schema) ? (schema + "." + tableName) : tableName;
    }

    /**
     * 初始化模型的表名和列名
     */
    public void init(ModelToTableConverter converter) {
        check();
        if (this.primaryKeyFields == null) {
            this.primaryKeyFields = new String[]{};
        }
        tableName = converter.getTableName(name, tableName);
        schema = converter.getSchemaName(schema);
        fields.forEach(t -> t.init(this, converter));
        if (tableDefine.getPartition() != null) {
            tableDefine.getPartition().init(this);
        }
        if (tableDefine.getOldTableNames() != null) {
            tableDefine.setOldTableNames(tableDefine.getOldTableNames().stream().map(converter::getWrappedIdentifierInMeta).collect(Collectors.toList()));
        }
    }

    /**
     * 检查合法性
     */
    private void check() {
        Assert.hasText(name, "模型名称不能为空");

        Assert.isTrue(!name.matches(".*[.{&`'\"\\n].*"), "模型'" + name + "'名含有点、引号或换行等非法字符");

        Assert.notEmpty(fields, "模型[" + name + "]字段不能为空");

        Set<String> set = new HashSet<>();
        for (Field field : fields) {
            field.check();

            String key = StringUtil.toUnderlineCase(field.getName());
            if (set.contains(key)) {
                throw new ModelException("模型[" + name + "]字段名[" + field.getName() + "]重复");
            }
            set.add(key);
        }
        Set<String> set2 = new HashSet<>();
        if (this.primaryKeyFields != null) {
            for (String primaryKeyField : this.primaryKeyFields) {
                if (findBasicField(primaryKeyField) == null) {
                    throw new ModelException("模型[" + name + "]主键字段[" + primaryKeyField + "]不存在");
                }
                if (set2.contains(primaryKeyField)) {
                    throw new ModelException("模型[" + name + "]主键字段[" + primaryKeyField + "]重复");
                }
                set2.add(primaryKeyField);
            }
        }
    }

    /**
     * 是否逻辑删除
     */
    @JsonIgnore
    public boolean isLogicDelete() {
        return findField(Model.FIELD_DELETE_FLAG) != null;
    }

    /**
     * 得到树形结构 parent 字段
     */
    @JsonIgnore
    public BasicField[] getParentIdFields() {
        ToOneField parentField = getParentField(true);
        if (parentField != null) {
            String[] parentIdFieldNames = parentField.getJoinLocalFields();
            BasicField[] result = new BasicField[parentIdFieldNames.length];
            for (int i = 0; i < parentIdFieldNames.length; i++) {
                result[i] = findBasicField(parentIdFieldNames[i]);
            }
            return result;
        }
        ToManyField childrenField = getChildrenField(true);
        if (childrenField != null) {
            String[] parentIdFieldNames = childrenField.getJoinTargetFields();
            BasicField[] result = new BasicField[parentIdFieldNames.length];
            for (int i = 0; i < parentIdFieldNames.length; i++) {
                result[i] = findBasicField(parentIdFieldNames[i]);
            }
            return result;
        }
        throw new RecursiveFieldException("模型[" + name + "]没有定义自关联字段。");
    }

    @JsonIgnore
    public void setPrimaryKeyField(String primaryKeyField) {
        this.primaryKeyFields = new String[]{primaryKeyField};
    }

    @JsonIgnore
    public void setComment(String comment) {
        this.tableDefine.setComment(comment);
    }

    /**
     * 主键字段
     */
    @JsonIgnore
    public List<BasicField> getPrimaryKeyFieldObjs() {
        if (primaryKeyFields == null) {
            return Collections.emptyList();
        }
        List<BasicField> list = new ArrayList<>();
        for (String primaryKeyField : primaryKeyFields) {
            list.add(findBasicField(primaryKeyField));
        }
        return list;
    }

    /**
     * 是否主键字段
     */
    public boolean isPrimaryKeyField(String fieldName) {
        if (primaryKeyFields == null) {
            return false;
        }
        for (String primaryKeyField : primaryKeyFields) {
            if (primaryKeyField.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public BasicField getFirstPrimaryKeyFieldObj() {
        if (primaryKeyFields == null || primaryKeyFields.length == 0) {
            return null;
        }
        return findBasicField(primaryKeyFields[0]);
    }

    /**
     * 得到树形结构 parent 字段
     */
    public ToOneField getParentField(boolean unFoundedReturnNull) {
        List<ToOneField> list = fields.stream()
                .filter(field -> field instanceof ToOneField).map(t -> (ToOneField) t)
                .filter(field -> Objects.equals(field.getTargetModel(), name))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            if (unFoundedReturnNull) {
                return null;
            }
            throw new ModelException("模型[" + name + "]子关联字段[" + name + "]不存在");
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            if (unFoundedReturnNull) {
                return null;
            }
            throw new ModelException("模型[" + name + "]子关联字段[" + name + "]不唯一");
        }
    }

    /**
     * 得到树形结构 children 字段
     */
    public ToManyField getChildrenField(boolean unFoundedReturnNull) {
        List<ToManyField> list = fields.stream()
                .filter(field -> field instanceof ToManyField).map(t -> (ToManyField) t)
                .filter(field -> Objects.equals(field.getTargetModel(), name))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            if (unFoundedReturnNull) {
                return null;
            }
            throw new RecursiveFieldException("模型[" + name + "]自关联一对多字段[" + name + "]不存在");
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            if (unFoundedReturnNull) {
                return null;
            }
            throw new RecursiveFieldException("模型[" + name + "]自关联一对多字段[" + name + "]不唯一");
        }
    }

    /**
     * 清除自定义配置的表名和列名
     */
    public void clearTableAndColumnName() {
        tableName = null;
        fields.forEach(t -> {
            if (t instanceof GroupField) {
                ((GroupField) t).clearTableAndColumnName();
            } else if (t instanceof BasicField) {
                ((BasicField) t).clearTableAndColumnName();
            }
        });
    }

    /**
     * 模型数据示例
     */
    public Map<String, Object> sample() {
        Map<String, Object> result = new LinkedHashMap<>();
        fields.forEach(field -> {
            if (!Model.FIELD_DELETE_FLAG.equals(field.getName())) {
                result.put(field.getName(), field.sampleData());
            }
        });
        return result;
    }

    @Override
    public Model clone() {
        try {
            Model clone = (Model) super.clone();
            clone.fields = fields.stream().map(Field::clone).collect(Collectors.toList());
            clone.extProperties = extProperties == null ? null : new LinkedHashMap<>(extProperties);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public Model addCommonFieldsIfNotExist(Class<?> idType) {
        if (String.class.equals(idType)) {
            addStringIdFieldIfNotExist();
        } else if (Integer.class.equals(idType)) {
            addIntegerIdFieldIfNotExist();
        } else if (Long.class.equals(idType)) {
            addLongIdFieldIfNotExist();
        } else {
            throw new ModelException("模型[" + name + "]配置错误，不支持的主键类型[" + idType + "]");
        }
        return addLogicDeleteFieldIfNotExist().addAuditFieldsIfNotExist();
    }

    public Model addStringIdFieldIfNotExist() {
        if (findField(Model.FIELD_ID) == null) {
            fields.add(createStringIdField());
        }
        this.setPrimaryKeyField(Model.FIELD_ID);
        return this;
    }

    public Model addIntegerIdFieldIfNotExist() {
        if (findField(Model.FIELD_ID) == null) {
            fields.add(createIntegerIdField());
        }
        this.setPrimaryKeyField(Model.FIELD_ID);
        return this;
    }

    public Model addLongIdFieldIfNotExist() {
        if (findField(Model.FIELD_ID) == null) {
            fields.add(createLongIdField());
        }
        this.setPrimaryKeyField(Model.FIELD_ID);
        return this;
    }

    public Model addLogicDeleteFieldIfNotExist() {
        if (!isLogicDelete()) {
            fields.add(createLogicDeleteField());
        }
        return this;
    }

    public Model addAuditFieldsIfNotExist() {
        if (findField(Model.FIELD_CREATOR) == null) {
            fields.add(Field.stringBuilder(FIELD_CREATOR).characterMaximumLength(64).fillerName(AbstractCreatorFiller.NAME).ddlComment("创建人").build());
        }
        if (findField(Model.FIELD_MODIFIER) == null) {
            fields.add(Field.stringBuilder(FIELD_MODIFIER).characterMaximumLength(64).fillerName(AbstractModifierFiller.NAME).ddlComment("修改人").build());
        }
        if (findField(Model.FIELD_CREATE_TIME) == null) {
            fields.add(Field.dateTimeBuilder(FIELD_CREATE_TIME).fillerName(CreateTimeFiller.NAME).ddlComment("创建时间").build());
        }
        if (findField(Model.FIELD_UPDATE_TIME) == null) {
            fields.add(Field.dateTimeBuilder(FIELD_UPDATE_TIME).fillerName(UpdateTimeFiller.NAME).ddlComment("修改时间").build());
        }
        return this;
    }

    public KeyGeneratorMode getUsedKeyGeneratorModel(DataBaseDialect dialect) {
        if (ObjectUtil.isEmpty(primaryKeyFields)) {
            return KeyGeneratorMode.NONE;
        }
        if (keyGeneratorMode == KeyGeneratorMode.DEFAULT) {
            if (primaryKeyFields.length > 1) {
                return KeyGeneratorMode.NONE;
            }
            BasicField pkField = getFirstPrimaryKeyFieldObj();
            if (pkField != null) {
                if (pkField.getJavaClass() == Integer.class || pkField.getJavaClass() == int.class || pkField.getJavaClass() == Long.class || pkField.getJavaClass() == long.class || pkField.getJavaClass() == BigInteger.class) {
                    if (dialect.supportAutoIncrement()) {
                        return KeyGeneratorMode.AUTO_INCREMENT;
                    } else if (dialect.supportSequence()) {
                        return KeyGeneratorMode.SEQUENCE;
                    }
                } else if (pkField.getJavaClass() == String.class) {
                    return KeyGeneratorMode.SNOWFLAKE;
                }
            }
        } else if (keyGeneratorMode != null) {
            return keyGeneratorMode;
        }
        return KeyGeneratorMode.NONE;
    }

    public static BasicField createStringIdField() {
        return Field.stringBuilder(FIELD_ID).characterMaximumLength(32).ddlComment("主键").build();
    }

    public static BasicField createIntegerIdField() {
        return Field.integerBuilder(FIELD_ID).ddlComment("主键").build();
    }

    public static BasicField createLongIdField() {
        return Field.longBuilder(FIELD_ID).ddlComment("主键").build();
    }

    public static BasicField createLogicDeleteField() {
        return Field.booleanBuilder(FIELD_DELETE_FLAG).select(false).ddlDefaultValue("0").ddlComment("逻辑删除").build();
    }

    public Field findField(String name) {
        for (Field field : getFields()) {
            if (Objects.equals(field.getName(), name)) {
                return field;
            }
        }
        return null;
    }

    public BasicField findBasicField(String name) {
        Field field = findField(name);
        if (field instanceof BasicField) {
            return (BasicField) field;
        }
        return null;
    }


}
