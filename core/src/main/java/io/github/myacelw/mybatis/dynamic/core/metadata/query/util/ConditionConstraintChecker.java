package io.github.myacelw.mybatis.dynamic.core.metadata.query.util;

import io.github.myacelw.mybatis.dynamic.core.exception.crud.ConditionPermissionException;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.*;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.*;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 查询条件约束检测器
 *
 * @author liuwei
 */
@Data
public class ConditionConstraintChecker {

    /**
     * 加入条件查询的字段权限，如果为空则表示不限制；
     * 这里的字段名需要设置关联后的字段路径， 比如 user.department.name
     */
    List<FieldConstraint> fieldConstraints;

    /**
     * 条件查询允许的逻辑操作，如果为空则表示不限制；
     */
    List<LogicOperation> logicOperations;

    /**
     * 是否允许自定义条件
     */
    boolean customConditionSupport;

    /**
     * 增加 字段约束
     *
     * @param fieldPath     字段路径
     * @param operationList 操作列表
     */
    public ConditionConstraintChecker add(@NonNull String fieldPath, ConditionOperation... operationList) {
        if (this.fieldConstraints == null) {
            this.fieldConstraints = new ArrayList<>();
        }
        this.fieldConstraints.add(new FieldConstraint(fieldPath, operationList == null ? null : Arrays.asList(operationList)));
        return this;
    }

    /**
     * 增加 逻辑操作约束
     *
     * @param operations 逻辑操作符列表
     */
    public ConditionConstraintChecker add(LogicOperation... operations) {
        if (operations != null) {
            if (this.logicOperations == null) {
                this.logicOperations = new ArrayList<>();
            }
            this.logicOperations.addAll(Arrays.asList(operations));
        }
        return this;
    }

    /**
     * 检查查询条件是否符合约束配置
     *
     * @param condition
     */
    public void check(Condition condition) {
        if (condition == null) {
            return;
        }
        if (condition instanceof GroupCondition) {
            GroupCondition c = (GroupCondition) condition;
            if (logicOperations == null || ObjectUtil.isEmpty(c.getConditions()) || logicOperations.stream().anyMatch(t -> t.name().equals(c.getLogic().name()))) {
                c.getConditions().forEach(this::check);
                return;
            }
            throw new ConditionPermissionException("没有逻辑操作符'" + c.getLogic() + "'查询条件权限");
        } else if (condition instanceof NotCondition) {
            NotCondition c = (NotCondition) condition;
            if (ObjectUtil.isEmpty(c.getCondition()) || logicOperations == null || logicOperations.contains(LogicOperation.not)) {
                check(c.getCondition());
                return;
            }
            throw new ConditionPermissionException("没有逻辑操作符'not'查询条件权限");
        } else if (condition instanceof ExistsCondition) {
            ExistsCondition c = (ExistsCondition) condition;
            if (fieldConstraints == null || fieldConstraints.stream().filter(t -> Objects.equals(t.getFieldPath(), c.getField())).anyMatch(t -> ObjectUtil.isEmpty(t.getOperationList()) || t.getOperationList().contains(ConditionOperation.exists))) {
                return;
            }
            throw new ConditionPermissionException("没有字段'" + c.getField() + "'的Exists查询条件权限");
        } else if (condition instanceof SimpleCondition) {
            SimpleCondition c = (SimpleCondition) condition;
            if (fieldConstraints == null || fieldConstraints.stream().filter(t -> Objects.equals(t.getFieldPath(), c.getField())).anyMatch(t -> ObjectUtil.isEmpty(t.getOperationList()) || t.getOperationList().stream().anyMatch(k -> k.name().equals(c.getOperation().name())))) {
                return;
            }
            throw new ConditionPermissionException("没有字段'" + c.getField() + "'的'" + c.getOperation() + "'查询条件权限");
        } else if (condition instanceof CustomCondition) {
            CustomCondition c = (CustomCondition) condition;
            if (!customConditionSupport) {
                throw new ConditionPermissionException("没有自定义查询条件权限");
            }

            if (fieldConstraints == null || fieldConstraints.stream().filter(t -> Objects.equals(t.getFieldPath(), c.getField())).anyMatch(t -> ObjectUtil.isEmpty(t.getOperationList()))) {
                return;
            }
            throw new ConditionPermissionException("没有字段'" + c.getField() + "'的查询条件权限");
        }
        throw new ConditionPermissionException("没有查询条件权限");
    }


    /**
     * 查询条件字段约束
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldConstraint {

        @NonNull
        String fieldPath;

        /**
         * 允许的条件操作符， 为空则不限制
         */
        List<ConditionOperation> operationList;
    }


    public enum LogicOperation {
        // ====
        and,
        or,

        not

    }

    public enum ConditionOperation {
        exists,

        // ====

        eq,
        lt,
        gt,
        lte,
        gte,
        ne,
        like,
        likeLeft,
        likeRight,
        isNotNull,
        isNull,
        isBlank,
        isNotBlank,
        in,
        eqOrIn,


    }


}
