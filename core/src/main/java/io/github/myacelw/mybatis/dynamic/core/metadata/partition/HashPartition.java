package io.github.myacelw.mybatis.dynamic.core.metadata.partition;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * hash分区
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HashPartition extends AbstractFieldPartition implements Cloneable {

    /**
     * 分区数量
     */
    private int partitionsNum;

    public HashPartition(String field, int partitionsNum) {
        this.field = field;
        this.partitionsNum = partitionsNum;
    }

    @Override
    public String getSql(boolean mainPartition) {
        String prefix = (mainPartition ? "" : "SUB");
        String sql = prefix + "PARTITION BY HASH(" + getFieldSql() + ")\n";

        if (mainPartition && this.getSubPartition() != null) {
            sql += this.getSubPartition().getSql(false);
        }

        sql += " " + prefix + "PARTITIONS " + partitionsNum;
        return sql;
    }


    @Override
    public HashPartition clone() {
        return (HashPartition) super.clone();
    }
}