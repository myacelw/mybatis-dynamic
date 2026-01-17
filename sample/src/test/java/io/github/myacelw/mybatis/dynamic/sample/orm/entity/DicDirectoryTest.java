package io.github.myacelw.mybatis.dynamic.sample.orm.entity;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.ToOneField;
import io.github.myacelw.mybatis.dynamic.core.service.impl.Class2ModelTransferImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DicDirectoryTest {

    @Test
    void test() {
        Class2ModelTransferImpl transfer = new Class2ModelTransferImpl();
        Model model = transfer.getModelForClass(DicDirectory.class);
        System.out.println(model);

        Field idField = model.findField("id");
        assertEquals(Integer.class, idField.getJavaClass());

        Field parentField = model.findField("parent");
        assertEquals(DicDirectory.class, parentField.getJavaClass());
        assertEquals(ToOneField.class, parentField.getClass());
    }
}