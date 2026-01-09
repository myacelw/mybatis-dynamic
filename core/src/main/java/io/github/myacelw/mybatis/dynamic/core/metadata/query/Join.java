package io.github.myacelw.mybatis.dynamic.core.metadata.query;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.util.lambda.SFunction;
import lombok.Data;
import lombok.SneakyThrows;

import static io.github.myacelw.mybatis.dynamic.core.util.lambda.LambdaUtil.name;

/**
 * Join查询设置。
 * 下面给一个典型模型场景并给出几个配置的例子：
 * 模型配置：
 * <br/>
 * 模型 User，字段 @ToOne Department department;
 * 模型 Department ，字段 @ToOne Company company; 字段 @ToMany List<User> users;
 * 模型 Company
 * <br/>
 * 示例1，查询User时关联 Department 和 Company，dataManager.query()接口中 joins 参数 配置如下：
 * Arrays.asList(Join.of("department"), Join.of("department.company"))
 * <br/>
 * 示例2，查询 Department 时关联查询部门下全部 User，joins 参数 配置如下：
 * Join.of("users")
 *
 * @author liuwei
 */
@Data
public class Join implements Cloneable {

    /**
     * 扩展为Join查询的字段路径
     */
    String fieldPath;

    /**
     * 扩展查询字段模型数据子查询的筛选条件，也就是Join On 上的附加条件。
     */
    Condition condition;

    /**
     * 关联类型
     */
    JoinType type = JoinType.LEFT;


    /**
     * 是否忽略逻辑删除
     */
    boolean ignoreLogicDelete = false;


    public Join ignoreLogicDelete() {
        this.ignoreLogicDelete = true;
        return this;
    }


    public static Join of(String fieldPath) {
        Join result = new Join();
        result.setFieldPath(fieldPath);
        return result;
    }

    public static Join of(SFunction<?, ?> field) {
        return of(name(field));
    }


    public static Join of(String fieldPath, JoinType type) {
        Join result = new Join();
        result.setFieldPath(fieldPath);
        if (type != null) {
            result.setType(type);
        }
        return result;
    }

    public static Join of(SFunction<?, ?> field, JoinType type) {
        return of(name(field), type);
    }


    @SneakyThrows
    @Override
    public Join clone() {
        return (Join) super.clone();
    }

    public enum JoinType {
        @JsonAlias({"Left", "left"})
        LEFT,
        @JsonAlias({"Inner", "inner"})
        INNER,
        @JsonAlias({"Right", "right"})
        RIGHT,
        @JsonAlias({"Full", "full"})
        FULL;

    }

}
