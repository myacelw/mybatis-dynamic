package io.github.myacelw.mybatis.dynamic.core.metadata.partition;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Key分区
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class KeyPartition extends AbstractFieldPartition implements Cloneable {

    /**
     * 分区数量
     */
    private int partitionsNum;

    public KeyPartition(String field, int partitionsNum) {
        this.field = field;
        this.partitionsNum = partitionsNum;
    }

    public KeyPartition(int partitionsNum) {
        this.partitionsNum = partitionsNum;
    }

    @Override
    public String getSql(boolean mainPartition) {
        String prefix = (mainPartition ? "" : "SUB");
        String sql = prefix + "PARTITION BY KEY(" + (StringUtil.hasText(this.field) ? this.field : "") + ")\n";

        if (mainPartition && this.getSubPartition() != null) {
            sql += this.getSubPartition().getSql(false);
        }

        sql += " " + prefix + "PARTITIONS " + partitionsNum;
        return sql;
    }

    @Override
    public KeyPartition convertFieldToColumn(Model model) {
        KeyPartition clone = this.clone();
        if(StringUtil.hasText(field)) {
            BasicField f = (BasicField) model.findField(field);
            Assert.notNull(f, "Partition field does not exist: " + field);
            clone.setField(f.getColumnName());
        }
        if (this.getSubPartition() != null) {
            clone.setSubPartition(this.getSubPartition().convertFieldToColumn(model));
        }
        return clone;
    }

    @Override
    public KeyPartition clone() {
        return (KeyPartition) super.clone();
    }

    @Override
    public void init(Model model) {
        if (subPartition != null) {
            subPartition.init(model);
        }
    }
}