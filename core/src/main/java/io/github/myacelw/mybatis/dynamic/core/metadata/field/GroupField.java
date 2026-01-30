package io.github.myacelw.mybatis.dynamic.core.metadata.field;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.service.ModelToTableConverter;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.*;

/**
 * 字段组合类型字段
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class GroupField extends AbstractField implements Serializable, Field {

    private static final long serialVersionUID = 1L;

    /**
     * 下级字段，如果是字段组或子表类型时填写
     */
    private List<BasicField> fields;

    /**
     * 是否需要查询
     */
    private Boolean select = true;

    @Override
    public void init(Model model, ModelToTableConverter converter) {
        Assert.notEmpty(fields, "Group fields cannot be empty");
        fields.forEach(t -> t.init(model, converter, name + "_"));
    }

    public void clearTableAndColumnName() {
        fields.forEach(BasicField::clearTableAndColumnName);
    }

    public void check() {
        super.check();
        //Assert.notEmpty(fields, "字段组下子字段不能为空");
    }

    public BasicField findField(String name) {
        for (BasicField field : fields) {
            if (Objects.equals(field.getName(), name)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public GroupField clone() {
        return (GroupField) super.clone();
    }

    @Override
    public Map<String, Object> sampleData() {
        Map<String, Object> result = new LinkedHashMap<>();
        fields.forEach(field -> result.put(field.getName(), field.sampleData()));
        return result;
    }


    public static class Builder extends AbstractField.Builder<GroupField, Builder> {

        public Builder(String name) {
            super(name, new GroupField());
            field.setFields(new ArrayList<>());
        }

        public GroupField.Builder fields(BasicField... subFields) {
            field.getFields().addAll(Arrays.asList(subFields));
            return self();
        }

        public GroupField.Builder fields(List<BasicField> subFields) {
            field.getFields().addAll(subFields);
            return self();
        }

        public GroupField.Builder field(BasicField subField) {
            field.getFields().add(subField);
            return self();
        }

    }

}
