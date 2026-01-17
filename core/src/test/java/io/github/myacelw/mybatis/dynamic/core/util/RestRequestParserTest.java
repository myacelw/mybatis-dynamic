package io.github.myacelw.mybatis.dynamic.core.util;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Page;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RestRequestParserTest {

    @Test
    public void testParseSimpleConditions() {
        Map<String, String[]> params = new HashMap<>();
        params.put("name", new String[]{"zhang"});
        params.put("age", new String[]{"gt:18"});
        params.put("status", new String[]{"in:1,2,3"});

        Condition condition = RestRequestParser.parseCondition(params);
        assertTrue(condition instanceof GroupCondition);
        GroupCondition gc = (GroupCondition) condition;
        assertEquals(3, gc.getConditions().size());

        // Verify name=zhang (eq)
        SimpleCondition nameCond = findCondition(gc, "name");
        assertEquals(SimpleCondition.Operation.eq, nameCond.getOperation());
        assertEquals("zhang", nameCond.getValue());

        // Verify age > 18 (gt)
        SimpleCondition ageCond = findCondition(gc, "age");
        assertEquals(SimpleCondition.Operation.gt, ageCond.getOperation());
        assertEquals("18", ageCond.getValue());

        // Verify status in (1, 2, 3) (in)
        SimpleCondition statusCond = findCondition(gc, "status");
        assertEquals(SimpleCondition.Operation.in, statusCond.getOperation());
        assertTrue(statusCond.getValue() instanceof List);
        assertEquals(Arrays.asList("1", "2", "3"), statusCond.getValue());
    }

    @Test
    public void testParsePage() {
        Map<String, String[]> params = new HashMap<>();
        params.put("page", new String[]{"2"});
        params.put("size", new String[]{"20"});

        Page page = RestRequestParser.parsePage(params);
        assertEquals(2, page.getCurrent());
        assertEquals(20, page.getSize());
    }

    @Test
    public void testParseSort() {
        Map<String, String[]> params = new HashMap<>();
        params.put("sort", new String[]{"age,desc", "name,asc"});

        List<OrderItem> orders = RestRequestParser.parseOrderItems(params);
        assertEquals(2, orders.size());
        assertEquals("age", orders.get(0).getField());
        assertFalse(orders.get(0).isAsc());
        assertEquals("name", orders.get(1).getField());
        assertTrue(orders.get(1).isAsc());
    }

    @Test
    public void testParseVariousOperators() {
        Map<String, String[]> params = new HashMap<>();
        params.put("f1", new String[]{"lt:10"});
        params.put("f2", new String[]{"gte:20"});
        params.put("f3", new String[]{"lte:30"});
        params.put("f4", new String[]{"ne:40"});
        params.put("f5", new String[]{"like:abc"});
        params.put("f6", new String[]{"contains:def"});
        params.put("f7", new String[]{"startsWith:ghi"});
        params.put("f8", new String[]{"endsWith:jkl"});
        params.put("f9", new String[]{"notIn:x,y"});
        params.put("f10", new String[]{"isNull"});
        params.put("f11", new String[]{"isNotNull"});
        params.put("f12", new String[]{"isBlank"});
        params.put("f13", new String[]{"isNotBlank"});

        Condition condition = RestRequestParser.parseCondition(params);
        GroupCondition gc = (GroupCondition) condition;

        assertEquals(SimpleCondition.Operation.lt, findCondition(gc, "f1").getOperation());
        assertEquals(SimpleCondition.Operation.gte, findCondition(gc, "f2").getOperation());
        assertEquals(SimpleCondition.Operation.lte, findCondition(gc, "f3").getOperation());
        assertEquals(SimpleCondition.Operation.ne, findCondition(gc, "f4").getOperation());
        assertEquals(SimpleCondition.Operation.like, findCondition(gc, "f5").getOperation());
        assertEquals(SimpleCondition.Operation.contains, findCondition(gc, "f6").getOperation());
        assertEquals(SimpleCondition.Operation.startsWith, findCondition(gc, "f7").getOperation());
        assertEquals(SimpleCondition.Operation.endsWith, findCondition(gc, "f8").getOperation());
        assertEquals(SimpleCondition.Operation.notIn, findCondition(gc, "f9").getOperation());
        assertEquals(SimpleCondition.Operation.isNull, findCondition(gc, "f10").getOperation());
        assertEquals(SimpleCondition.Operation.isNotNull, findCondition(gc, "f11").getOperation());
        assertEquals(SimpleCondition.Operation.isBlank, findCondition(gc, "f12").getOperation());
        assertEquals(SimpleCondition.Operation.isNotBlank, findCondition(gc, "f13").getOperation());
    }

    @Test
    public void testReservedParamsIgnored() {
        Map<String, String[]> params = new HashMap<>();
        params.put("page", new String[]{"1"});
        params.put("size", new String[]{"10"});
        params.put("sort", new String[]{"id,desc"});
        params.put("selectFields", new String[]{"id,name"});
        params.put("other", new String[]{"val"});

        Condition condition = RestRequestParser.parseCondition(params);
        GroupCondition gc = (GroupCondition) condition;
        assertEquals(1, gc.getConditions().size());
        assertEquals("other", ((SimpleCondition)gc.getConditions().get(0)).getField());
    }

    private SimpleCondition findCondition(GroupCondition gc, String field) {
        return gc.getConditions().stream()
                .filter(c -> c instanceof SimpleCondition)
                .map(c -> (SimpleCondition) c)
                .filter(c -> field.equals(c.getField()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Condition not found for field: " + field));
    }
}
