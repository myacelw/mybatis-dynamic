package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.annotation.IdField;
import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import io.github.myacelw.mybatis.dynamic.core.annotation.ToOne;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ToOneMissingFkTest extends BaseExecutionTest {

    @Model(name = "MissingFkDepartment")
    public static class MissingFkDepartment {
        @IdField
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Model(name = "MissingFkEmployee")
    public static class MissingFkEmployee {
        @IdField
        private String id;
        private String name;

        @ToOne(targetModel = "MissingFkDepartment")
        private MissingFkDepartment department;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public MissingFkDepartment getDepartment() {
            return department;
        }

        public void setDepartment(MissingFkDepartment department) {
            this.department = department;
        }
    }


    @Test
    void testQueryWithMissingFk() {
        modelService.updateAndRegister(MissingFkDepartment.class);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            modelService.updateAndRegister(MissingFkEmployee.class);
        });

        String message = exception.getMessage();
        assertTrue(message.contains("departmentId"));
        assertTrue(message.contains("not found in model"));
        assertTrue(message.contains("Please explicitly define this field with @BasicField"));
    }
}
