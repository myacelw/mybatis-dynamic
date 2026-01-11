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

    @Test
    public void testCustomConditionClone() {
        CustomCondition original = CustomCondition.of("template", new String[]{"f1", "f2"}, "val");
        CustomCondition cloned = original.clone();

        assertNotSame(original, cloned);
        assertNotSame(original.getFields(), cloned.getFields()); // Array should be cloned
        assertArrayEquals(original.getFields(), cloned.getFields());
        assertEquals(original.getSqlTemplate(), cloned.getSqlTemplate());
        assertEquals(original.getValue(), cloned.getValue());
    }

    @Test
    public void testSearchConditionClone() {
        SearchCondition original = SearchCondition.of("field", "val");
        SearchCondition cloned = original.clone();

        assertNotSame(original, cloned);
        assertEquals(original, cloned);
    }

    @Test
    public void testExistsConditionClone() {
        SimpleCondition inner = SimpleCondition.eq("id", 1);
        ExistsCondition original = ExistsCondition.of("field", inner);
        ExistsCondition cloned = original.clone();

        assertNotSame(original, cloned);
        assertNotSame(original.getCondition(), cloned.getCondition()); // Deep copy check
        assertEquals(original.getCondition(), cloned.getCondition());
    }
}
