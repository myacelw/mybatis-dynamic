package io.github.myacelw.mybatis.dynamic.core.metadata.query;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.lambda.SFunction;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static io.github.myacelw.mybatis.dynamic.core.util.lambda.LambdaUtil.name;

/**
 * 排序
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
public class OrderItem {

    /**
     * 需要进行排序的字段；如果是聚合查询结果做排序，则使用"$F[0]、$F[1]" 表示对应的聚合字段
     */
    private String field;
    /**
     * 是否正序排列，默认 true
     */
    private boolean asc = true;

    /**
     * 函数模板，用于函数结果排序。
     * 例如 OceanBase 数据库向量检索排序：ORDER BY l2_distance($COL, #{EXPR}) APPROXIMATE LIMIT 3 表示对向量距离函数结果的排序
     * 其中的 $COL 会被替换为 列； EXPR 会被替换为函数值
     */
    private String functionTemplate;

    /**
     * 函数参数。
     */
    private Object functionValue;

    /**
     * 创建一个按指定字段升序排序的 OrderItem 对象
     *
     * @param field 需要进行排序的字段名，不能为 null
     * @return 一个表示升序排序的 OrderItem 对象
     */
    public static OrderItem asc(@NonNull String field) {
        OrderItem t = new OrderItem();
        t.field = field;
        return t;
    }

    /**
     * 创建一个按指定字段升序排序的 OrderItem 对象
     *
     * @param field 用于指定排序字段的 Lambda 表达式，应为实体的属性get方法，不能为 null
     * @return 一个表示升序排序的 OrderItem 对象
     */
    public static <T> OrderItem asc(@NonNull SFunction<T, ?> field) {
        return asc(name(field));
    }

    /**
     * 创建一个按指定字段降序排序的 OrderItem 对象
     *
     * @param field 需要进行排序的字段名，不能为 null
     * @return 一个表示降序排序的 OrderItem 对象
     */
    public static OrderItem desc(@NonNull String field) {
        OrderItem t = new OrderItem();
        t.field = field;
        t.asc = false;
        return t;
    }

    /**
     * 创建一个按指定字段降序排序的 OrderItem 对象
     *
     * @param field 用于指定排序字段的 Lambda 表达式，应为实体的属性get方法，不能为 null
     * @return 一个表示降序排序的 OrderItem 对象
     */
    public static <T> OrderItem desc(@NonNull SFunction<T, ?> field) {
        return desc(name(field));
    }

    public String sql(FieldToSqlConverter fieldToSqlConverter, String valueExpression, DataBaseDialect dialect) {
        Assert.hasText(field, "field must not be null");

        String result;
        if (functionTemplate == null) {
            String column = fieldToSqlConverter.convertColumn(field);
            result = column;
        } else {
            result = functionTemplate;
            if (valueExpression != null) {
                result = result.replace("EXPR", valueExpression);
            }
            if(result.contains("$COL")){
                String column = fieldToSqlConverter.convertColumn(field);
                result = result.replaceAll("\\$COL", column);
            } else {
                result = field;
            }

        }
        if (!asc) {
            result = result + " DESC";
        }
        return result;
    }
}
