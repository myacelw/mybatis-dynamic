package io.github.myacelw.mybatis.dynamic.core.metadata.partition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 列表分区
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ListPartition<T> extends AbstractFieldPartition implements Cloneable {

    /**
     * List 类型列表值
     */
    private List<Part<T>> parts;

    /**
     * 默认分区名 ， 注意Mysql数据库不支持，Oceanbase支持。
     */
    private String defaultPartitionName;

    @Override
    public String getSql(boolean mainPartition) {
        String prefix = (mainPartition ? "" : "SUB");

        //这里的 field实际上已经被转换为了列，因此没有问题
        String sql = prefix + "PARTITION BY LIST COLUMNS(" + getFieldSql() + ")\n";
        if (mainPartition && this.getSubPartition() != null) {
            sql += this.getSubPartition().getSql(false);
        }
        if (!mainPartition) {
            sql += " SUBPARTITION TEMPLATE ";
        }
        sql += "(\n" +
               String.join(",\n", parts.stream().map(t -> t.getSql(mainPartition)).collect(Collectors.toList()));

        if (defaultPartitionName != null) {
            sql += ",\n" + prefix + "PARTITION " + defaultPartitionName + " VALUES IN ( DEFAULT )\n";
        }
        sql += ")";
        return sql;
    }

    public static <T> ListPartition<T> of(String field, List<List<T>> list, boolean haveDefaultPartition, boolean mainPartition) {
        ListPartition<T> partition = new ListPartition<>();
        partition.setField(field);
        List<Part<T>> parts = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<T> values = list.get(i);
            parts.add(new Part<>((mainPartition ? "P" : "SP") + i, values));
        }
        partition.setParts(parts);
        if (haveDefaultPartition) {
            partition.setDefaultPartitionName((mainPartition ? "P" : "SP") + "_DEFAULT");
        }
        return partition;
    }

    @Override
    public ListPartition<T> clone() {
        ListPartition<T> clone = (ListPartition<T>) super.clone();
        this.parts = new ArrayList<>(clone.parts);
        return clone;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Part<T> {
        String name;
        List<T> values;

        public String getSql(boolean mainPartition) {
            String s = values.stream().map(t -> {
                        String p = t instanceof Number ? "" : "'";
                        return p + t + p;
                    }
            ).collect(Collectors.joining(","));

            return String.format((mainPartition ? "" : "SUB") + "PARTITION %s VALUES IN (%s)", name, s);
        }
    }
}