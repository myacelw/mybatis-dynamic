package io.github.myacelw.mybatis.dynamic.core.metadata.query.condition;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionTest {

    @Test
    void match() {

        Map<String, Object> data = new HashMap<>();
        data.put("name", "zhangsan");
        data.put("age", 18);
        data.put("height", 180.5f);
        data.put("isMarried", true);
        data.put("birthday", "1990-01-01");
        data.put("gender", "男");
        data.put("abc", null);

        Condition c11 = Condition.builder().startsWith("name", "zhang").build();
        assertTrue(c11.match(data));

        Condition c12 = Condition.builder().lt("age", 20).build();
        assertTrue(c12.match(data));

        Condition c13 = Condition.builder().eq("gender", "男").build();
        assertTrue(c13.match(data));

        Condition c14 = Condition.builder().gt("height", 170f).build();
        assertTrue(c14.match(data));

        Condition c15 = Condition.builder().lt("age", 20).eqOrInOptional("gender", null).build();
        assertTrue(c15.match(data));

        Condition c16 = Condition.builder().lt("age", 20).eqOrInOptional("gender", Arrays.asList("男", "女")).build();
        assertTrue(c16.match(data));

        Condition c17 = Condition.builder().lt("age", 20).eqOrInOptional("gender", Collections.singletonList("女")).build();
        assertFalse(c17.match(data));

        Condition c18 = Condition.builder().eqOrInOptional("gender", null).build();
        assertNull(c18.match(data));

        Condition c19 = Condition.builder().eq("abc", null).build();
        assertFalse(c19.match(data));

        Condition c20 = Condition.builder().isBlank("abc").build();
        assertTrue(c20.match(data));

        Condition c1 = Condition.builder().startsWith("name", "zhang").lt("age", 20).eq("gender", "男").gt("height", 170f).build();
        assertTrue(c1.match(data));

        Condition c2 = Condition.builder().contains("name", "zhang").gt("age", 20).eq("gender", "男").gt("height", 170f).build();
        assertFalse(c2.match(data));

        Condition c3 = Condition.builder().or(b -> b.contains("name", "zhang").gt("age", 20).eq("gender", "男").gt("height", 170f)).build();
        assertTrue(c3.match(data));

    }
}