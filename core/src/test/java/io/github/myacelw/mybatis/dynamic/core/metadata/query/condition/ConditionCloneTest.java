package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConditionCloneTest {

    @Test
    public void testConditionIsCloneable() {
        assertTrue(Cloneable.class.isAssignableFrom(Condition.class), "Condition should extend Cloneable");
    }

    @Test
    public void testSimpleConditionClone() {
        SimpleCondition original = SimpleCondition.eq("name", "test");
        SimpleCondition cloned = original.clone();

        assertNotSame(original, cloned);
        assertEquals(original, cloned);
        assertEquals(original.getField(), cloned.getField());
        assertEquals(original.getValue(), cloned.getValue());
    }
}
