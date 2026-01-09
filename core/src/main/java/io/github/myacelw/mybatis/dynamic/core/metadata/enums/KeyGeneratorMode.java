package io.github.myacelw.mybatis.dynamic.core.metadata.enums;

/**
 * 主键生成方式
 *
 * @author liuwei
 */
public enum KeyGeneratorMode {
    /**
     * 默认自动推断，int或long类型使用自增或序列，String类型使用雪花算法；多主键情况情况为None。
     */
    DEFAULT,
    /**
     * 雪花算法
     */
    SNOWFLAKE,
    /**
     * 自增主键
     */
    AUTO_INCREMENT,
    /**
     * 数据库序列
     */
    SEQUENCE,
    /**
     * 关闭自动生成
     */
    NONE,
}
