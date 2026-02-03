package io.github.myacelw.mybatis.dynamic.core.metadata.query;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JoinTest {

    @Test
    public void testFluentApi() {
        Join join = Join.of("dept")
                .on(c -> c.eq("active", true).and().gt("age", 18));

        Condition condition = join.getCondition();
        Assertions.assertTrue(condition instanceof GroupCondition);
        GroupCondition gc = (GroupCondition) condition;
        Assertions.assertEquals(GroupCondition.Logic.AND, gc.getLogic());
        Assertions.assertEquals(2, gc.getConditions().size());
    }
    
    @Test
    public void testOnAnd() {
        Join join = Join.of("dept").on(Condition.builder().eq("a", 1).and().eq("b", 2).build());
        Assertions.assertTrue(join.getCondition() instanceof GroupCondition);
        GroupCondition gc = (GroupCondition) join.getCondition();
        Assertions.assertEquals(2, gc.getConditions().size());
    }

    @Test
    public void testOr() {
        Join join = Join.of("dept")
                .on(c -> c.eq("a", 1).or().eq("b", 2));
                
        Assertions.assertTrue(join.getCondition() instanceof GroupCondition);
        GroupCondition gc = (GroupCondition) join.getCondition();
        Assertions.assertEquals(GroupCondition.Logic.OR, gc.getLogic());
    }

    @Test
    public void testInner() {
        Join join = Join.inner("dept");
        Assertions.assertEquals("dept", join.getFieldPath());
        Assertions.assertEquals(Join.JoinType.INNER, join.getType());
    }

    @Test
    public void testLeft() {
        Join join = Join.left("dept");
        Assertions.assertEquals("dept", join.getFieldPath());
        Assertions.assertEquals(Join.JoinType.LEFT, join.getType());
    }

    @Test
    public void testRight() {
        Join join = Join.right("dept");
        Assertions.assertEquals("dept", join.getFieldPath());
        Assertions.assertEquals(Join.JoinType.RIGHT, join.getType());
    }
}
