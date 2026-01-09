package io.github.myacelw.mybatis.dynamic.core.metadata.partition;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 抽象的字段分区
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractFieldPartition extends Partition implements Cloneable {

    /**
     * 分区字段
     */
    protected String field;

    /**
     * 分区字段
     */
    protected String fieldSqlTemplate;


    protected String getFieldSql() {
        if (fieldSqlTemplate != null) {
            //这里的 field实际上已经被转换为了列，因此没有问题
            return Condition.replacePlaceholders(null, null, fieldSqlTemplate, field);
        }
        return field;
    }

    @Override
    public List<String> getFields() {
        Set<String> fields = new LinkedHashSet<>();
        fields.add(field);
        if (this.getSubPartition() != null) {
            fields.addAll(this.getSubPartition().getFields());
        }
        return new ArrayList<>(fields);
    }

    @Override
    public AbstractFieldPartition convertFieldToColumn(Model model) {
        Assert.notNull(field, "分区字段不能为空");
        BasicField f = (BasicField) model.findField(field);
        Assert.notNull(f, "分区字段不存在：" + field);
        AbstractFieldPartition clone = this.clone();
        clone.setField(f.getColumnName());
        if (this.getSubPartition() != null) {
            clone.setSubPartition(this.getSubPartition().convertFieldToColumn(model));
        }
        return clone;
    }

    /**
     * 初始化分区设置对模型的影响。
     */
    @Override
    public void init(Model model) {
        Field partField = model.findField(field);
        Assert.notNull(partField, "分区字段不存在：" + field);
        if (subPartition != null) {
            subPartition.init(model);
        }
    }

    @Override
    public AbstractFieldPartition clone() {
        return (AbstractFieldPartition) super.clone();
    }
}