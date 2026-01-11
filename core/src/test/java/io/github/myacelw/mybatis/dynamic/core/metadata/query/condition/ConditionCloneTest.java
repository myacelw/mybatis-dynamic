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
        assertNotSame(original.getCondition(), cloned.getCondition());
        assertEquals(original.getCondition(), cloned.getCondition());
    }

    @Test
    public void testNotConditionClone() {
        SimpleCondition inner = SimpleCondition.eq("name", "test");
        NotCondition original = NotCondition.of(inner);
        NotCondition cloned = original.clone();

        assertNotSame(original.getCondition(), cloned.getCondition());
        assertEquals(original.getCondition(), cloned.getCondition());
    }

    @Test
    public void testGroupConditionClone() {
        SimpleCondition c1 = SimpleCondition.eq("f1", 1);
        SimpleCondition c2 = SimpleCondition.eq("f2", 2);
        GroupCondition original = GroupCondition.and(c1, c2);
        GroupCondition cloned = original.clone();

        assertNotSame(original, cloned);
        assertNotSame(original.getConditions(), cloned.getConditions()); // List should be cloned
        assertEquals(original.getConditions().size(), cloned.getConditions().size());
        
        for (int i = 0; i < original.getConditions().size(); i++) {
            assertNotSame(original.getConditions().get(i), cloned.getConditions().get(i)); // Elements should be cloned
            assertEquals(original.getConditions().get(i), cloned.getConditions().get(i));
        }
    }
}
