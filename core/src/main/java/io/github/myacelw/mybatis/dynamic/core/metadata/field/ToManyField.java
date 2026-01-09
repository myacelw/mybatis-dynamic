package io.github.myacelw.mybatis.dynamic.core.metadata.field;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.service.ModelToTableConverter;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 一对多字段。
 *
 * @author liuwei
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ToManyField extends AbstractField implements RefModel, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联模型名称，必填
     */
    protected String targetModel;

    /**
     * 关联模型的ManyToOne字段名称，必填
     */
    protected String[] joinTargetFields;

    @Override
    public void init(Model model, ModelToTableConverter converter) {
        // nothing
    }

    @Override
    public void check() {
        super.check();
        Assert.notNull(targetModel, "关联模型的字段不能为空");
        Assert.notEmpty(joinTargetFields, "关联模型的字段不能为空");
        Assert.hasText(joinTargetFields[0], "关联模型的字段不能为空");
        Assert.hasText(targetModel, "关联模型字段名不能为空");
    }

    public void setJoinTargetField(String joinTargetField) {
        this.joinTargetFields = new String[]{joinTargetField};
    }

    @Override
    public List<Object> sampleData() {
        return null;
    }

    @Override
    public ToManyField clone() {
        return (ToManyField) super.clone();
    }


    public static class Builder extends AbstractField.Builder<ToManyField, Builder> {

        public Builder(String name, String targetModel, String joinTargetField) {
            super(name, new ToManyField());
            field.setTargetModel(targetModel);
            field.setJoinTargetFields(new String[]{joinTargetField});
        }

        public Builder(String name, String targetModel, String... joinTargetFields) {
            super(name, new ToManyField());
            field.setTargetModel(targetModel);
            field.setJoinTargetFields(joinTargetFields);
        }

    }

}
