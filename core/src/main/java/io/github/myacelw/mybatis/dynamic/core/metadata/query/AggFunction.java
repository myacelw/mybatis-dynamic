package io.github.myacelw.mybatis.dynamic.core.metadata.query;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;

import java.util.List;

/**
 * 聚合函数
 *
 * @author liuwei
 */
public enum AggFunction {

    /**
     * 不汇总
     */
    NONE("$COL") {
        public Class<?> getJavaType(Class<?> javaType) {
            return javaType != null ? javaType : Object.class;
        }
    },

    COUNT("COUNT($COL)") {
        public Class<?> getJavaType(Class<?> javaType) {
            return javaType != null && Number.class.isAssignableFrom(javaType) ? javaType : Integer.class;
        }
    },

    SUM("SUM($COL)") {
        public Class<?> getJavaType(Class<?> javaType) {
            return javaType != null && Number.class.isAssignableFrom(javaType) ? javaType : Double.class;
        }
    },

    AVG("AVG($COL)") {
        public Class<?> getJavaType(Class<?> javaType) {
            return javaType != null && Number.class.isAssignableFrom(javaType) ? javaType : Double.class;
        }
    },

    MAX("MAX($COL)") {
        public Class<?> getJavaType(Class<?> javaType) {
            return javaType != null ? javaType : Double.class;
        }
    },

    MIN("MIN($COL)") {
        public Class<?> getJavaType(Class<?> javaType) {
            return javaType != null ? javaType : Double.class;
        }
    },

    COUNT_DISTINCT("COUNT(DISTINCT $COL)") {
        public Class<?> getJavaType(Class<?> javaType) {
            return javaType != null && Number.class.isAssignableFrom(javaType) ? javaType : Integer.class;
        }
    },

    LISTAGG("GROUP_CONCAT($COL)") {
        public Class<?> getJavaType(Class<?> javaType) {
            return String.class;
        }

        public String toSelectColumn(String column, DataBaseDialect dialect) {
            return dialect.getListAggFunctionSql(column);
        }
    },

    LISTAGG_DISTINCT("GROUP_CONCAT(DISTINCT $COL)") {
        public Class<?> getJavaType(Class<?> javaType) {
            return String.class;
        }

        public String toSelectColumn(String column, DataBaseDialect dialect) {
            return dialect.getListAggDistinctFunctionSql(column);
        }
    },

    JSON_ARRAYAGG("JSON_ARRAYAGG($COL)") {
        public Class<?> getJavaType(Class<?> javaType) {
            return List.class;
        }

        public String toSelectColumn(String column, DataBaseDialect dialect) {
            return dialect.getJsonArrayAggFunctionSql(column);
        }
    },

    JSON_ARRAYAGG_DISTINCT("JSON_ARRAYAGG(DISTINCT $COL)") {
        public Class<?> getJavaType(Class<?> javaType) {
            return List.class;
        }

        public String toSelectColumn(String column, DataBaseDialect dialect) {
            return dialect.getJsonArrayAggDistinctFunctionSql(column);
        }
    },


    CUSTOM("$COL") {
        public Class<?> getJavaType(Class<?> javaType) {
            return javaType != null ? javaType : Object.class;
        }
    },

    ;

    private final String template;

    AggFunction(String template) {
        this.template = template;
    }

    public String toSelectColumn(String column, DataBaseDialect dialect) {
        return template.replaceAll("\\$COL", column) ;
    }

    public abstract Class<?> getJavaType(Class<?> javaType);
}
