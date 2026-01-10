package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FluentConditionTest {

    @Test
    void testLogicPrecedence_AndThenOr() {
        // a=1 and b=2 or c=3  => (a=1 AND b=2) OR c=3
        Condition c = Condition.builder()
                .eq("a", 1)
                .and().eq("b", 2)
                .or().eq("c", 3)
                .build();
        
        // GroupCondition.toString() output format: LOGIC(c1, c2, ...)
        assertEquals("OR(AND(eq(a, 1), eq(b, 2)), eq(c, 3))", c.toString());
    }

    @Test
    void testLogicPrecedence_OrThenAnd() {
        // a=1 or b=2 and c=3 => a=1 OR (b=2 AND c=3)
        Condition c = Condition.builder()
                .eq("a", 1)
                .or().eq("b", 2)
                .and().eq("c", 3)
                .build();
        
        assertEquals("OR(eq(a, 1), AND(eq(b, 2), eq(c, 3)))", c.toString());
    }

    @Test
    void testLogicPrecedence_Complex() {
        // a=1 and b=2 or c=3 and d=4 => (a=1 AND b=2) OR (c=3 AND d=4)
        Condition c = Condition.builder()
                .eq("a", 1)
                .and().eq("b", 2)
                .or().eq("c", 3)
                .and().eq("d", 4)
                .build();
        
        assertEquals("OR(AND(eq(a, 1), eq(b, 2)), AND(eq(c, 3), eq(d, 4)))", c.toString());
    }

    @Test
    void testExplicitBrackets() {
        // (a=1 or b=2) and c=3
        Condition c = Condition.builder()
                .bracket(b -> b.eq("a", 1).or().eq("b", 2))
                .and().eq("c", 3)
                .build();
        
        assertEquals("AND(OR(eq(a, 1), eq(b, 2)), eq(c, 3))", c.toString());
    }
    
    @Test
    void testNot() {
        // not a=1 and b=2
        Condition c = Condition.builder()
                .not().eq("a", 1)
                .and().eq("b", 2)
                .build();
        
        assertEquals("AND(NOT(eq(a, 1)), eq(b, 2))", c.toString());
    }

    @Test
    void testNotBracket() {
        // not (a=1 or b=2)
        Condition c = Condition.builder()
                .not(b -> b.eq("a", 1).or().eq("b", 2))
                .build();
        
        assertEquals("AND(NOT(OR(eq(a, 1), eq(b, 2))))", c.toString());
    }

    @Test
    void testBaseMethods() {
        ConditionBuilder cb;
        
        cb = Condition.builder();
        assertEquals("AND(eq(a, 1))", cb.eq("a", 1).build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(ne(a, 1))", cb.ne("a", 1).build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(gt(a, 1))", cb.gt("a", 1).build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(lt(a, 1))", cb.lt("a", 1).build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(gte(a, 1))", cb.gte("a", 1).build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(lte(a, 1))", cb.lte("a", 1).build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(like(a, %v%))", cb.like("a", "%v%").build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(contains(a, v))", cb.contains("a", "v").build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(startsWith(a, v))", cb.startsWith("a", "v").build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(endsWith(a, v))", cb.endsWith("a", "v").build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(isNull(a, null))", cb.isNull("a").build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(isNotNull(a, null))", cb.isNotNull("a").build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(isBlank(a, null))", cb.isBlank("a").build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(isNotBlank(a, null))", cb.isNotBlank("a").build().toString());
    }

    @Test
    void testInMethods() {
        ConditionBuilder cb;
        List<Integer> list = java.util.Arrays.asList(1, 2, 3);
        
        cb = Condition.builder();
        assertEquals("AND(in(a, [1, 2, 3]))", cb.in("a", list).build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(notIn(a, [1, 2, 3]))", cb.notIn("a", list).build().toString());
        
        cb = Condition.builder();
        assertEquals("AND(eqOrIn(a, [1, 2, 3]))", cb.eqOrIn("a", list).build().toString());
    }

    @Test
    void testExists() {
        ConditionBuilder cb = Condition.builder();
        
        // exists(field, consumer)
        cb.exists("user", b -> b.eq("name", "zhangsan"));
        // ExistsCondition.toString() format is "EXISTS(field, condition)"
        assertEquals("AND(EXISTS(user, AND(eq(name, zhangsan))))", cb.build().toString());
        
        cb = Condition.builder();
        cb.not().exists("user", b -> b.eq("name", "zhangsan"));
        assertEquals("AND(NOT(EXISTS(user, AND(eq(name, zhangsan)))))", cb.build().toString());
    }
}
