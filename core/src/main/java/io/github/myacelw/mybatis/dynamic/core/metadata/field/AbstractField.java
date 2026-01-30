package io.github.myacelw.mybatis.dynamic.core.metadata.field;

import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 字段抽象类
 *
 * @author liuwei
 */
@Data
public abstract class AbstractField implements Field {

    /**
     * 字段名，必填
     */
    protected String name;

    /**
     * 字段java类型，必填
     */
    protected Class<?> javaClass;

    /**
     * 任意扩展属性
     */
    protected Map<String, Object> extProperties;

    /**
     * 检查合法性
     */
    @Override
    public void check() {
        Assert.hasText(name, "Field name cannot be empty");
        Assert.isTrue(!name.matches(".*[.{&`'\"\\n].*"), "Field name contains illegal characters (dots, quotes, or newlines)");
    }

    @Override
    public AbstractField clone() {
        try {
            AbstractField clone = (AbstractField) super.clone();
            clone.extProperties = extProperties == null ? null : new LinkedHashMap<>(extProperties);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


    public static class Builder<R extends AbstractField, T extends Builder<R, ?>> {
        final R field;

        public Builder(String name, R field) {
            this.field = field;
            this.field.setName(name);
        }

        public R build() {
            return field;
        }

        public T javaClass(Class<?> javaClass) {
            field.setJavaClass(javaClass);
            return self();
        }

        public T extProperty(String key, Object value) {
            field.putExtProperty(key, value);
            return self();
        }

        T self() {
            return (T) this;
        }

    }

}
