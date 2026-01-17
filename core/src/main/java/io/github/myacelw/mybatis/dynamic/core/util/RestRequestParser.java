package io.github.myacelw.mybatis.dynamic.core.util;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;

import java.util.*;

/**
 * REST 请求参数解析工具类
 * 将 URL 查询参数解析为 mybatis-dynamic 的查询条件、分页和排序对象。
 */
public class RestRequestParser {

    private static final Set<String> RESERVED_PARAMS = new HashSet<>(Arrays.asList("page", "size", "sort", "selectFields"));

    public static Condition parseCondition(Map<String, String[]> params) {
        GroupCondition root = new GroupCondition();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            if (RESERVED_PARAMS.contains(key)) {
                continue;
            }

            String[] values = entry.getValue();
            if (values == null || values.length == 0) {
                continue;
            }

            for (String val : values) {
                root.getConditions().add(parseSimpleCondition(key, val));
            }
        }
        return root;
    }

    private static SimpleCondition parseSimpleCondition(String field, String value) {
        if (value == null) {
            return SimpleCondition.eq(field, null);
        }

        if (value.startsWith("gt:")) {
            return SimpleCondition.gt(field, value.substring(3));
        } else if (value.startsWith("lt:")) {
            return SimpleCondition.lt(field, value.substring(3));
        } else if (value.startsWith("ge:") || value.startsWith("gte:")) {
            return SimpleCondition.gte(field, value.substring(value.indexOf(":") + 1));
        } else if (value.startsWith("le:") || value.startsWith("lte:")) {
            return SimpleCondition.lte(field, value.substring(value.indexOf(":") + 1));
        } else if (value.startsWith("ne:")) {
            return SimpleCondition.ne(field, value.substring(3));
        } else if (value.startsWith("like:")) {
            return SimpleCondition.like(field, value.substring(5));
        } else if (value.startsWith("contains:")) {
            return SimpleCondition.contains(field, value.substring(9));
        } else if (value.startsWith("startsWith:")) {
            return SimpleCondition.startsWith(field, value.substring(11));
        } else if (value.startsWith("endsWith:")) {
            return SimpleCondition.endsWith(field, value.substring(9));
        } else if (value.startsWith("in:")) {
            return SimpleCondition.in(field, Arrays.asList(value.substring(3).split(",")));
        } else if (value.startsWith("notIn:")) {
            return SimpleCondition.notIn(field, Arrays.asList(value.substring(6).split(",")));
        } else if ("isNull".equals(value)) {
            return SimpleCondition.isNull(field);
        } else if ("isNotNull".equals(value)) {
            return SimpleCondition.isNotNull(field);
        } else if ("isBlank".equals(value)) {
            return SimpleCondition.isBlank(field);
        } else if ("isNotBlank".equals(value)) {
            return SimpleCondition.isNotBlank(field);
        } else {
            return SimpleCondition.eq(field, value);
        }
    }

    public static Page parsePage(Map<String, String[]> params) {
        Page page = new Page();
        String[] pageVal = params.get("page");
        if (pageVal != null && pageVal.length > 0) {
            try {
                page.setCurrent(Integer.parseInt(pageVal[0]));
            } catch (NumberFormatException ignored) {}
        }
        String[] sizeVal = params.get("size");
        if (sizeVal != null && sizeVal.length > 0) {
            try {
                page.setSize(Integer.parseInt(sizeVal[0]));
            } catch (NumberFormatException ignored) {}
        }
        return page;
    }

    public static List<OrderItem> parseOrderItems(Map<String, String[]> params) {
        List<OrderItem> orders = new ArrayList<>();
        String[] sortVals = params.get("sort");
        if (sortVals != null) {
            for (String sortVal : sortVals) {
                String[] parts = sortVal.split(",");
                if (parts.length > 0) {
                    String field = parts[0];
                    boolean asc = true;
                    if (parts.length > 1) {
                        asc = !"desc".equalsIgnoreCase(parts[1]);
                    }
                    OrderItem item = new OrderItem();
                    item.setField(field);
                    item.setAsc(asc);
                    orders.add(item);
                }
            }
        }
        return orders;
    }
}
