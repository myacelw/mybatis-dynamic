package io.github.myacelw.mybatis.dynamic.core.metadata.query;

import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import io.github.myacelw.mybatis.dynamic.core.util.lambda.SFunction;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static io.github.myacelw.mybatis.dynamic.core.util.lambda.LambdaUtil.name;

/**
 * 汇总查询结果项
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
public class AggSelectItem {

    public final static AggSelectItem COUNT = new AggSelectItem() {
        {
            field = "*";
            aggFunction = AggFunction.COUNT;
            propertyName = "count";
        }

        public void setField(String field) {
            throw new UnsupportedOperationException("static COUNT is readonly");
        }

        public void setAggFunction(AggFunction aggFunction) {
            throw new UnsupportedOperationException("static COUNT is readonly");
        }

        public void setPropertyName(String propertyName) {
            throw new UnsupportedOperationException("static COUNT is readonly");
        }
    };

    /**
     * 字段名
     */
    @NonNull
    String field;

    /**
     * 汇总函数
     */
    AggFunction aggFunction = AggFunction.NONE;

    String propertyName;

    /**
     * 自定义函数，其中 $COL 表示字段名
     */
    String customFunction;

    /**
     * 自定义函数，返回类型
     */
    Class<?> javaType;

    /**
     * 如果使用此列排序，则设置排序方向
     */
    private Boolean orderAsc;

    /**
     * 检查合法性
     */
    public void check() {
        Assert.hasText(field, "Field name cannot be empty");
        if (StringUtil.hasText(propertyName)) {
            Assert.hasText(propertyName, "Property name cannot be empty");
            Assert.isTrue(!propertyName.matches(".*[`'\"\\n].*"), "Property name contains illegal characters (quotes or newlines)");
        }
    }


    public static AggSelectItem of(String field) {
        AggSelectItem result = new AggSelectItem();
        result.setField(field);
        return result;
    }

    public static <T> AggSelectItem of(SFunction<T, ?> field) {
        return of(name(field));
    }

    public static AggSelectItem of(String field, AggFunction aggFunction) {
        AggSelectItem result = new AggSelectItem();
        result.setField(field);
        result.setAggFunction(aggFunction);
        return result;
    }

    public static AggSelectItem of(String field, String propertyName) {
        AggSelectItem result = new AggSelectItem();
        result.setField(field);
        result.setPropertyName(propertyName);
        return result;
    }

    public static AggSelectItem of(String field, AggFunction aggFunction, String propertyName) {
        AggSelectItem result = of(field, aggFunction);
        result.setPropertyName(propertyName);
        return result;
    }

    public static AggSelectItem customOf(String field, String customFunction, Class<?> javaType, String propertyName) {
        AggSelectItem result = new AggSelectItem();
        result.setField(field);
        result.setAggFunction(AggFunction.CUSTOM);
        result.setPropertyName(propertyName);
        result.setCustomFunction(customFunction);
        result.setJavaType(javaType);
        return result;
    }

    public static AggSelectItem customOfGroupBy(String field, String customFunction, Class<?> javaType, String propertyName) {
        AggSelectItem result = new AggSelectItem();
        result.setField(field);
        result.setAggFunction(AggFunction.NONE);
        result.setPropertyName(propertyName);
        result.setCustomFunction(customFunction);
        result.setJavaType(javaType);
        return result;
    }


    public static <T> AggSelectItem of(SFunction<T, ?> field, AggFunction aggFunction) {
        return of(name(field), aggFunction);
    }


}
