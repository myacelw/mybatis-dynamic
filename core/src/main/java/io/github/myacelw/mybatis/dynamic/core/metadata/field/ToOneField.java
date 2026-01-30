package io.github.myacelw.mybatis.dynamic.core.metadata.field;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.service.ModelToTableConverter;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 关联关系类型（多对一关系）字段
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ToOneField extends AbstractField implements RefModel, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联模型名称，必填
     */
    protected String targetModel;

    /**
     * 关联字段名，必填
     */
    protected String[] joinLocalFields;


    @Override
    public void init(Model model, ModelToTableConverter converter) {

    }

    @Override
    public void check() {
        super.check();
        Assert.hasText(targetModel, "Target model name cannot be empty");
        Assert.notEmpty(joinLocalFields, "Join local fields cannot be empty");
        Assert.hasText(joinLocalFields[0], "Join local fields cannot be empty");
    }

    public void setJoinLocalField(String joinLocalField) {
        this.joinLocalFields = new String[]{joinLocalField};
    }

    @Override
    public Object sampleData() {
        return null;
    }

    @Override
    public ToOneField clone() {
        return (ToOneField) super.clone();
    }


    public static class Builder extends AbstractField.Builder<ToOneField, Builder> {

        public Builder(String name, String relModelName) {
            super(name, new ToOneField());
            field.setTargetModel(relModelName);
        }

        public Builder joinLocalField(String relFieldName) {
            field.setJoinLocalField(relFieldName);
            return self();
        }

        public Builder joinLocalFields(String... joinLocalFields) {
            field.setJoinLocalFields(joinLocalFields);
            return self();
        }


    }

}
