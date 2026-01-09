package io.github.myacelw.mybatis.dynamic.core.metadata.partition;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;

/**
 * 分区工厂
 *
 * @author liuwei
 */
public interface PartitionFactory {

    Partition create(String field, String[] params, boolean mainPartition);

    class None implements PartitionFactory {

        @Override
        public Partition create(String field, String[] params, boolean mainPartition) {
            return null;
        }
    }

    class Key implements PartitionFactory {
        @Override
        public Partition create(String field,String[] params, boolean mainPartition) {
            int partitionsNum = Integer.parseInt(params[0]);
            return new KeyPartition(partitionsNum);
        }
    }
    class Hash implements PartitionFactory {

        @Override
        public Partition create(String field,String[] params, boolean mainPartition) {
            int partitionsNum = Integer.parseInt(params[0]);
            return new HashPartition(field, partitionsNum);
        }
    }

    class DateRange implements PartitionFactory {
        @Override
        public Partition create(String field, String[] params, boolean mainPartition) {
            LocalDate lowDate = LocalDate.parse(params[0]);
            LocalDate maxDate = LocalDate.parse(params[1]);
            RangePartition.DateUnit dateUnit = RangePartition.DateUnit.valueOf(params[2]);
            return RangePartition.of(field,lowDate,maxDate,dateUnit,mainPartition);
        }
    }

    class List implements PartitionFactory {
        ObjectMapper objectMapper = new ObjectMapper();
        @Override
        public Partition create(String field, String[] params, boolean mainPartition) {
            java.util.List values = objectMapper.convertValue(params[0], java.util.List.class);
            boolean haveDefaultPartition = Boolean.parseBoolean(params[1]);
            return ListPartition.of(field,values,haveDefaultPartition, mainPartition);
        }
    }

}
