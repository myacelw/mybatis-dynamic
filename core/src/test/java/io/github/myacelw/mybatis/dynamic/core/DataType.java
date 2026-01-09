package io.github.myacelw.mybatis.dynamic.core;

import lombok.Getter;

/**
 * 数据库数据类型。
 *
 * @author liuwei
 */
@Getter
public enum DataType {
    NONE,

    //通用类型
    CHAR,
    VARCHAR,
    NUMERIC,
    DECIMAL,
    DATE,
    TIMESTAMP,
    INTEGER,

    //非用用类型
    DATETIME(), //MYSQL
    CLOB(false), //ORACLE
    TEXT(true), //MYSQL, POSTGRESQL，OCEANBASE
    MEDIUMTEXT(true), //MYSQL，OCEANBASE
    LONGTEXT(true), //MYSQL，OCEANBASE
    BLOB(false), //MYSQL, ORACLE
    MEDIUMBLOB(false), //MYSQL
    LONGBLOB(false), //MYSQL
    BYTEA(false), //POSTGRESQL

    JSON(true), //MYSQL, POSTGRESQL
    JSONB(false), //POSTGRESQL

    TINYINT, //MYSQL
    SMALLINT, //MYSQL, POSTGRESQL
    INT, //MYSQL, POSTGRESQL
    MEDIUMINT, //MYSQL
    BIGINT, //MYSQL, POSTGRESQL

    /**
     * 向量类型
     */
    VECTOR(true), //POSTGRESQL，OCEANBASE
    ;

    private final boolean canIndex;

    DataType() {
        this(true);
    }

    DataType(boolean canIndex) {
        this.canIndex = canIndex;
    }


    public boolean isNumber() {
        return this == INTEGER || this == TINYINT || this == SMALLINT || this == INT || this == MEDIUMINT || this == BIGINT || this == NUMERIC || this == DECIMAL;
    }

    public boolean isString() {
        return this == CHAR || this == VARCHAR || this == TEXT || this == MEDIUMTEXT || this == LONGTEXT || this == CLOB;
    }

}
