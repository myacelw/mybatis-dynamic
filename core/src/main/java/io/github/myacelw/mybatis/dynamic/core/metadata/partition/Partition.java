package io.github.myacelw.mybatis.dynamic.core.metadata.partition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import lombok.Data;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 分区配置
 *
 * @author liuwei
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RangePartition.class, name = "Range", names = {"range", "RANGE"}),
        @JsonSubTypes.Type(value = ListPartition.class, name = "List", names = {"list", "LIST"}),
        @JsonSubTypes.Type(value = HashPartition.class, name = "Hash", names = {"hash", "HASH"}),
        @JsonSubTypes.Type(value = KeyPartition.class, name = "Key", names = {"key", "KEY"}),
})
@Data
public abstract class Partition implements Cloneable{
    /**
     * 子分区配置（二级分区）
     */
    protected Partition subPartition;

    public abstract String getSql(boolean mainPartition);

    public abstract List<String> getFields();

    public abstract Partition convertFieldToColumn(Model model);

    public static void main(String[] args) {
        RangePartition<LocalDate> result = RangePartition.of("create_time", LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1), RangePartition.DateUnit.MONTH, true);
        result.setSubPartition(ListPartition.of("status", Arrays.asList(Arrays.asList(1, 2), Arrays.asList(3, 4)), true, false));

        System.out.println(result.getSql(true));
    }

    @Override
    public Partition clone() {
        try {
            Partition clone = (Partition) super.clone();
            clone.subPartition = this.subPartition==null? null: this.subPartition.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public abstract void init(Model model);
}