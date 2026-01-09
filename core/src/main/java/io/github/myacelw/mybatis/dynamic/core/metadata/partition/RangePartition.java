package io.github.myacelw.mybatis.dynamic.core.metadata.partition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 范围分区
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RangePartition<T> extends AbstractFieldPartition implements Cloneable {

    /**
     * 范围上限值（不含）
     */
    private List<Part<T>> parts;

    public String getSql(boolean mainPartition) {
        String prefix = (mainPartition ? "" : "SUB");

        String sql = prefix + "PARTITION BY RANGE COLUMNS(" + getFieldSql() + ") ";

        if (mainPartition && this.getSubPartition() != null) {
            sql += this.getSubPartition().getSql(false);
        }

        if (!mainPartition) {
            sql += " SUBPARTITION TEMPLATE ";
        }

        sql += "(\n" +
               String.join(",\n", parts.stream().map(t -> t.getSql(true)).collect(Collectors.toList())) +
               "\n)";

        return sql;
    }

    /**
     * 根据日期范围生成分区
     *
     * @param field    字段名
     * @param lowDate  下线日期，如： 2024-01-01
     * @param maxDate  上线日期，如： 2025-01-01
     * @param dateUnit 时间单位，如： 年，月，日，季度，半年，半个月
     * @return 分区
     */
    public static RangePartition<LocalDate> of(String field, LocalDate lowDate, LocalDate maxDate, DateUnit dateUnit, boolean mainPartition) {
        RangePartition<LocalDate> partition = new RangePartition<>();
        partition.setField(field);

        // 根据时间单位生成不同间隔的日期序列
        List<Part<LocalDate>> partList = new ArrayList<>();

        // 创建日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        LocalDate current = lowDate;
        while (!current.isAfter(maxDate)) {
            Part<LocalDate> part = new Part<>();
            part.setRangeLessThanValue(current);
            part.setName((mainPartition ? "P" : "SP") + dateUnit.prev(current).format(formatter));
            partList.add(part);

            current = dateUnit.next(current);
        }

        partition.setParts(partList);
        return partition;
    }

    @Override
    public RangePartition<T> clone() {
        RangePartition<T> clone = (RangePartition<T>) super.clone();
        this.parts = new ArrayList<>(clone.parts);
        return clone;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Part<T> {
        String name;
        T rangeLessThanValue;

        public String getSql(boolean mainPartition) {
            String p = rangeLessThanValue instanceof Number ? "" : "'";
            return String.format((mainPartition ? "" : "SUB") + "PARTITION %s VALUES LESS THAN (%s%s%s)", name, p, rangeLessThanValue, p);
        }
    }

    public enum DateUnit {
        YEAR {
            @Override
            public LocalDate next(LocalDate date) {
                return date.plusYears(1);
            }

            @Override
            public LocalDate prev(LocalDate date) {
                return date.minusYears(1);
            }
        },
        MONTH {
            @Override
            public LocalDate next(LocalDate date) {
                return date.plusMonths(1);
            }

            @Override
            public LocalDate prev(LocalDate date) {
                return date.minusMonths(1);
            }
        },
        DAY {
            @Override
            public LocalDate next(LocalDate date) {
                return date.plusDays(1);
            }

            @Override
            public LocalDate prev(LocalDate date) {
                return date.minusDays(1);
            }
        },
        QUARTER {
            @Override
            public LocalDate next(LocalDate date) {
                return date.plusMonths(3);
            }

            @Override
            public LocalDate prev(LocalDate date) {
                return date.minusMonths(3);
            }
        },
        //半年
        HALF_YEAR {
            @Override
            public LocalDate next(LocalDate date) {
                return date.plusMonths(6);
            }

            @Override
            public LocalDate prev(LocalDate date) {
                return date.minusMonths(6);
            }
        },
        //半个月
        HALF_MONTH {
            @Override
            public LocalDate next(LocalDate date) {
                if (date.getDayOfMonth() > 1) {
                    return date.plusMonths(1).withDayOfMonth(1);
                } else {
                    return date.withDayOfMonth(16);
                }
            }

            @Override
            public LocalDate prev(LocalDate date) {
                if (date.getDayOfMonth() > 1) {
                    return date.withDayOfMonth(1);
                } else {
                    return date.minusMonths(1).withDayOfMonth(16);
                }
            }
        };


        public abstract LocalDate next(LocalDate date);

        public abstract LocalDate prev(LocalDate date);

    }


}