package io.github.myacelw.mybatis.dynamic.core.service.impl;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.MysqlDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.exception.model.ModelException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.AlterOrDropStrategy;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.KeyGeneratorMode;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.GroupField;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.service.ModelToTableConverter;
import io.github.myacelw.mybatis.dynamic.core.service.handler.*;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 模型表格转换器实现类
 *
 * @author liuwei
 */
@Slf4j
public class ModelToTableConverterImpl implements ModelToTableConverter {

    private final DataBaseDialect dialect;

    private final String tablePrefix;

    private final String seqPrefix;

    private final String indexPrefix;


    private final List<ColumnTypeHandler> columnTypeHandlers;

    private final static ColumnTypeHandler[] DEFAULT_COLUMN_TYPE_HANDLERS = new ColumnTypeHandler[]{
            new StringColumnTypeHandler(),
            new CharColumnTypeHandler(),
            new ByteColumnTypeHandler(),
            new BooleanColumnTypeHandler(),
            new ShortColumnTypeHandler(),
            new IntegerColumnTypeHandler(),
            new LongColumnTypeHandler(),
            new FloatColumnTypeHandler(),
            new DoubleColumnTypeHandler(),
            new ClassColumnTypeHandler(),
            new EnumColumnTypeHandler(),
            new BigIntegerColumnTypeHandler(),
            new BigDecimalColumnTypeHandler(),
            new LocalDateColumnTypeHandler(),
            new LocalDateTimeColumnTypeHandler(),
            new MapOrCollectionColumnTypeHandler(),
            new BytesColumnTypeHandler(),
            new FloatArrayColumnTypeHandler(),
            new ObjectColumnTypeHandler()
    };

    public ModelToTableConverterImpl(DataBaseDialect dialect, String tablePrefix, String seqPrefix, String indexPrefix, List<ColumnTypeHandler> columnTypeHandlers) {
        this.dialect = dialect == null ? new MysqlDataBaseDialect() : dialect;
        this.tablePrefix = tablePrefix == null ? "" : tablePrefix;
        this.seqPrefix = seqPrefix == null ? "" : seqPrefix;
        this.indexPrefix = indexPrefix == null ? "" : indexPrefix;
        this.columnTypeHandlers = new ArrayList<>();
        if (columnTypeHandlers != null) {
            this.columnTypeHandlers.addAll(columnTypeHandlers);
        }
        this.columnTypeHandlers.addAll(Arrays.asList(DEFAULT_COLUMN_TYPE_HANDLERS));
    }

    /**
     * @param dialect 数据库方言
     */
    public ModelToTableConverterImpl(DataBaseDialect dialect) {
        this(dialect, "", "", "", null);
    }

    /**
     * 模型名转换为表名，表名拥有统一前缀
     */
    @Override
    public String getTableName(@NonNull String modelName, String customTableName) {
        if (StringUtil.hasText(customTableName)) {
            return customTableName.replace("${tablePrefix}", tablePrefix);
        }
        return wrapper(tablePrefix + StringUtil.toUnderlineCase(modelName)).toLowerCase();
    }

    /**
     * 字段称转换为列名
     */
    @Override
    public String getColumnName(@NonNull String fieldName, String customColumnName) {
        if (StringUtil.hasText(customColumnName)) {
            return reWrapper(customColumnName);
        }
        return wrapper(StringUtil.toUnderlineCase(fieldName)).toLowerCase();
    }

    /**
     * 索引名转换
     */
    @Override
    public String getIndexName(@NonNull String tableName, @NonNull String columnName, String customIndexName) {
        if (StringUtil.hasText(customIndexName)) {
            return customIndexName.replace("${indexPrefix}", indexPrefix);
        }
        //注意：表有可能改名，此时创建的索引前缀中的表名可能和当前表名不一致
        String name;
        if (dialect instanceof MysqlDataBaseDialect) {
            name = indexPrefix + dialect.unWrapper(columnName);
        } else {
            name = indexPrefix + dialect.unWrapper(tableName) + "_" + dialect.unWrapper(columnName);
        }

        if (name.length() > 64) {
            name = name.substring(0, 60) + Integer.toHexString(name.hashCode()).substring(0, 4);
        }
        return name.toLowerCase();
    }


    /**
     * 存在中文等非法字符，或者为数据库关键字时进行包装处理
     */
    protected String wrapper(String name) {
        return this.dialect.wrapper(name);
    }

    @Override
    public String reWrapper(String name) {
        return wrapper(dialect.unWrapper(name));
    }

    /**
     * Model转换为Table。
     * 字段如果是子表则会生成多个Table，因此返回Map，Map的key是字段名。
     *
     * @param fieldWhiteList 在此名单中的字段才涉及变更，空时不限制。
     */
    @Override
    public Table convertToTable(@NonNull Model model, List<String> fieldWhiteList) {
        Table table = new Table(model.getTableName(), model.getSchema());
        table.setOldTableNames(model.getTableDefine().getOldTableNames());
        table.setDisableAlterComment(model.getTableDefine().getDisableAlterComment());
        table.setComment(getComment(model.getTableDefine().getComment()));
        table.setExtProperties(model.getExtProperties());
        if (model.getTableDefine().getPartition() != null) {
            table.setPartition(model.getTableDefine().getPartition().convertFieldToColumn(model));
        }

        setPk(model, table);

        addDropColumns(table, model.getTableDefine().getDropColumnNames());
        addColumns(model, table, model.getFields(), fieldWhiteList);

        return table;
    }

    private void setPk(Model model, Table table) {
        List<String> pkList = new ArrayList<>();
        String[] primaryKeyFieldNames = model.getPrimaryKeyFields();
        for (String primaryKeyFieldName : primaryKeyFieldNames) {
            BasicField basicField = model.findBasicField(primaryKeyFieldName);
            assert basicField != null;
            pkList.add(basicField.getColumnName());
        }
        table.setPrimaryKeyColumns(pkList);
        if (!pkList.isEmpty()) {
            table.setKeyGeneratorColumn(pkList.get(0));
        }

        table.setKeyGeneratorMode(model.getUsedKeyGeneratorModel(dialect));

        if (table.getKeyGeneratorMode() == KeyGeneratorMode.SEQUENCE) {
            if (StringUtil.hasText(model.getTableDefine().getKeyGeneratorSequenceName())) {
                table.setKeyGeneratorSequenceName(model.getTableDefine().getKeyGeneratorSequenceName());
            } else {
                table.setKeyGeneratorSequenceName(seqPrefix + table.getTableName());
            }
        }
    }

    /**
     * 处理特殊指定的移除列
     */
    protected void addDropColumns(Table table, List<String> dropColumnNameList) {
        if (dropColumnNameList != null) {
            for (String columnName : dropColumnNameList) {
                Column c = new Column();
                c.setColumnName(columnName);
                c.setAlterOrDropStrategy(AlterOrDropStrategy.DROP);
                table.getColumns().add(c);
            }
        }

    }

    protected void addColumns(@NonNull Model model, Table table, Collection<? extends Field> fields, List<String> fieldWhiteList) {
        if (fields != null) {
            for (Field field : fields) {
                boolean white = fieldWhiteList == null || fieldWhiteList.contains(field.getName());
                if (field instanceof GroupField) {
                    GroupField groupField = (GroupField) field;
                    groupField.getFields().forEach(subField -> {
                        if (white || fieldWhiteList.contains(field.getName() + "." + subField.getName())) {
                            Column column = convertColumn(model, subField);
                            table.getColumns().add(column);
                        }
                    });
                } else if (white && field instanceof BasicField) {
                    Column column = convertColumn(model, (BasicField) field);
                    if (model.isPrimaryKeyField(field.getName())) {
                        column.setNotNull(true);
                    }
                    table.getColumns().add(column);
                }
            }
        }
    }

    protected Column convertColumn(Model model, BasicField field) {
        Column column = new Column();
        column.setColumnName(field.getColumnName());
        column.setComment(getComment(field.getColumnDefinition().getComment()));
        column.setOldColumnNames(field.getColumnDefinition().getOldColumnNames());
        column.setDisableAlterComment(field.getColumnDefinition().getDisableAlterComment());
        column.setAdditionalDDl(field.getColumnDefinition().getAdditionalDDl());
        setColumnType(model, field, column);
        column.setNotNull(field.getColumnDefinition().getNotNull());
        column.setDefaultValue(field.getColumnDefinition().getDefaultValue());
        column.setAlterOrDropStrategy(getStrategy(field));
        column.setIndex(field.getColumnDefinition().getIndex() == Boolean.TRUE);
        column.setIndexName(field.getColumnDefinition().getIndexName());
        column.setIndexType(field.getColumnDefinition().getIndexType());
        return column;
    }

    private String getComment(String comment) {
        return comment == null ? null : comment.replaceAll("['#$\n]", "");
    }

    private AlterOrDropStrategy getStrategy(BasicField field) {
        AlterOrDropStrategy strategy = field.getColumnDefinition().getAlterOrDropStrategy();

        return strategy == null ? AlterOrDropStrategy.ALTER : strategy;
    }

    private void setColumnType(Model model, BasicField field, Column column) {
        for (ColumnTypeHandler columnTypeHandler : columnTypeHandlers) {
            boolean b = columnTypeHandler.doSetColumnType(column, field.getJavaClass(), dialect, field);
            if (b) {
                break;
            }
        }

        String customDataType = field.getColumnDefinition().getColumnType();
        if (customDataType != null) {
            column.setDataType(customDataType);
        } else if (field.getJdbcType() != null) {
            column.setDataType(field.getJdbcType().toString());
        }

        if (column.getDataType() == null) {
            throw new ModelException("模型[" + model.getName() + "]的字段[" + field.getName() + "]的类型[" + field.getJavaClass() + "]不能转换为数据库类型");
        }

        Class typeHandler = field.getTypeHandlerClass();
        if (typeHandler != null) {
            column.setTypeHandler(typeHandler);
        }

        Integer customCharacterMaximumLength = field.getColumnDefinition().getCharacterMaximumLength();
        if (customCharacterMaximumLength != null) {
            column.setCharacterMaximumLength(customCharacterMaximumLength);
        }

        Integer customNumericPrecision = field.getColumnDefinition().getNumericPrecision();
        if (customNumericPrecision != null) {
            column.setNumericPrecision(customNumericPrecision);
        }

        Integer customNumericScale = field.getColumnDefinition().getNumericScale();
        if (customNumericScale != null) {
            column.setNumericScale(customNumericScale);
        }

        column.setCustomIndexColumn(field.getColumnDefinition().getCustomIndexColumn());

        Integer customVectorLength = field.getExtPropertyValueForInteger(Field.EXT_PROPERTY_COLUMN_VECTOR_LENGTH);
        if (customNumericScale != null) {
            column.setVectorLength(customVectorLength);
        }

        if (!"VARCHAR".equalsIgnoreCase(column.getDataType()) && !"CHAR".equalsIgnoreCase(column.getDataType())) {
            column.setCharacterMaximumLength(null);
        }
        if (!"DECIMAL".equalsIgnoreCase(column.getDataType()) && !"NUMERIC".equalsIgnoreCase(column.getDataType())) {
            column.setNumericScale(null);
            column.setNumericPrecision(null);
        }

    }

}
