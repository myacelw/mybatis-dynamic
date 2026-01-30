package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.MysqlDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.OceanBaseDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.PostgresqlDataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.FieldToSqlConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 全文检索条件
 * 例如，分词检索示例如下：
 * sqlTemplate = "MATCH ($COL) AGAINST (#{EXPR} IN BOOLEAN MODE)"
 * field = "doc"
 * value = "+贸易"
 *
 * @author liuwei
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchCondition implements Condition {

    /**
     * 字段名
     */
    String field;

    /**
     * 取值
     */
    Object value;

    /**
     * 查询模式
     */
    Mode mode = Mode.DEFAULT;


    @Override
    public String toString() {
        return "SEARCH_" + mode + "(" + field + ", " + value + ")";
    }


    @JsonIgnore
    public Object getV() {
        return value;
    }

    @Override
    public SearchCondition clone() {
        try {
            return (SearchCondition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String sql(String valueExpression, FieldToSqlConverter fieldToSqlConverter, DataBaseDialect dialect) {
        String sqlTemplate = getSqlTemplate(dialect);
        return Condition.replacePlaceholders(valueExpression, fieldToSqlConverter, sqlTemplate, field);
    }

    protected String getSqlTemplate(DataBaseDialect dialect) {
        if (dialect instanceof OceanBaseDataBaseDialect || dialect instanceof MysqlDataBaseDialect) {
            if (mode == Mode.BOOLEAN) {
                return "MATCH ($COL) AGAINST (#{EXPR} IN BOOLEAN MODE)";
            } else {
                return "MATCH ($COL) AGAINST (#{EXPR} IN NATURAL LANGUAGE MODE)";
            }
        } else if (dialect instanceof PostgresqlDataBaseDialect) {
            if (mode == Mode.BOOLEAN) {
                throw new UnsupportedOperationException("Full-text search BOOLEAN mode is not supported for database type: " + dialect.getName());
            }
            return "to_tsvector('chinese', $COL) @@ to_tsquery('chinese', #{EXPR})";
        } else {
            throw new UnsupportedOperationException("Full-text search is not supported for database type: " + dialect.getName());
        }
    }

    @Override
    public List<String> innerGetSimpleConditionFields() {
        return Collections.singletonList(field);
    }

    /**
     * 查询模式
     */
    public enum Mode {
        //IN NATURAL LANGUAGE MODE
        DEFAULT,
        //IN BOOLEAN MODE
        BOOLEAN,
        ;
    }

    public static SearchCondition of(String field, Object value) {
        SearchCondition condition = new SearchCondition();
        condition.setField(field);
        condition.setValue(value);
        return condition;
    }

    public static SearchCondition ofBoolMode(String field, Object value) {
        SearchCondition condition = of(field, value);
        condition.setMode(Mode.BOOLEAN);
        return condition;
    }

}
