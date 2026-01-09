package io.github.myacelw.mybatis.dynamic.core.util.lambda;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LambdaUtilTest {

    @Test
    void name() {
        String s = LambdaUtil.name(Model::getName);
        assertEquals("name", s);
    }
}