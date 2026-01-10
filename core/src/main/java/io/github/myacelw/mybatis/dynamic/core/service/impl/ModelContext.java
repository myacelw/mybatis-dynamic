package io.github.myacelw.mybatis.dynamic.core.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.myacelw.mybatis.dynamic.core.database.MybatisHelper;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.event.Event;
import io.github.myacelw.mybatis.dynamic.core.event.EventListener;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.Permission;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.GroupField;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.GroupCondition;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.SimpleCondition;
import io.github.myacelw.mybatis.dynamic.core.service.DataChangeInterceptor;
import io.github.myacelw.mybatis.dynamic.core.service.DataManagerGetter;
import io.github.myacelw.mybatis.dynamic.core.service.filler.Filler;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 模型上下文
 *
 * @author liuwei
 */
@Slf4j
public class ModelContext {
    @Getter
    private final Model model;

    @Getter
    private final DataBaseDialect dialect;

    @Getter
    private final MybatisHelper mybatisHelper;

    @Getter
    private final DataManagerGetter dataManagerGetter;

    @Getter
    private final List<EventListener> eventListeners;

    @Getter
    private final Map<String, Filler> fillers;

    @Getter
    private final DataChangeInterceptorGroup interceptor;

    @Getter
    private final Map<String, Field> fieldMap;

    @Getter
    private final Map<String, Field> fieldRights;

    @Getter
    private final Permission permission;

    @Getter
    private final Condition additionalCondition;

    @Getter
    private final Condition additionalIgnoreDeleteCondition;

    @Getter
    private final SqlSession sqlSession;


    public ModelContext(Model model, DataBaseDialect dialect, MybatisHelper mybatisHelper, DataManagerGetter dataManagerGetter, Map<String, Filler> fillers, Permission permission, List<EventListener> eventListeners, List<DataChangeInterceptor> interceptors, SqlSession sqlSession) {
        this.model = model;
        this.dialect = dialect;
        this.mybatisHelper = mybatisHelper;
        this.permission = permission;
        this.fillers = fillers;
        this.eventListeners = eventListeners;
        this.interceptor = new DataChangeInterceptorGroup(interceptors == null ? Collections.emptyList() : interceptors);
        this.dataManagerGetter = dataManagerGetter;
        this.sqlSession = sqlSession;

        this.fieldMap = createFieldMap(model.getFields());

        Condition deleteFlagCondition = isLogicDelete() ? SimpleCondition.eq(Model.FIELD_DELETE_FLAG, 0) : null;

        this.additionalCondition = permission != null ? GroupCondition.and(deleteFlagCondition, permission.getDataRights()) : deleteFlagCondition;
        this.additionalIgnoreDeleteCondition = permission != null ? permission.getDataRights() : null;
        this.fieldRights = createFieldRights();
    }

    public Field getField(String fieldName) {
        return fieldMap.get(fieldName);
    }

    /**
     * 是否逻辑删除
     */
    public boolean isLogicDelete() {
        return getField(Model.FIELD_DELETE_FLAG) != null;
    }

    public BasicField getDeleteFlagField(){
        return (BasicField) getField(Model.FIELD_DELETE_FLAG);
    }

    private Map<String, Field> createFieldMap(List<Field> fields) {
        Map<String, Field> fieldMap = new LinkedHashMap<>();
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
        return Collections.unmodifiableMap(fieldMap);
    }

    private Map<String, Field> createFieldRights() {
        if (permission == null || permission.getFieldRights() == null) {
            return fieldMap;
        }
        Map<String, Field> fieldRights = permission.getFieldRights().stream().map(fieldMap::get).filter(Objects::nonNull)
                .collect(Collectors.toMap(Field::getName, f -> f));
        if (model.getPrimaryKeyFields() != null) {
            for (String primaryKeyField : model.getPrimaryKeyFields()) {
                fieldRights.put(primaryKeyField, fieldMap.get(primaryKeyField));
            }
        }
        return Collections.unmodifiableMap(fieldRights);
    }

    public Collection<Field> getPermissionFields() {
        return fieldRights.values();
    }

    public Field getField(String fieldName, boolean permission, boolean unfoundThrowException) {
        if (permission) {
            return getPermissionedField(fieldName, unfoundThrowException);
        }
        Field field = fieldMap.get(fieldName);
        if (unfoundThrowException) {
            Assert.notNull(field, "模型'" + getModel().getName() + "'的字段'" + fieldName + "'不存在");
        }
        return field;
    }

    public Field getPermissionedField(String fieldName) {
        if (fieldName.contains(".")) {
            String[] parts = fieldName.split("\\.");
            Field field = fieldRights.get(parts[0]);
            if (field instanceof GroupField) {
                return ((GroupField) field).findField(parts[1]);
            } else {
                return null;
            }
        } else {
            return fieldRights.get(fieldName);
        }
    }

    public Field getPermissionedField(String fieldName, boolean unfoundThrowException) {
        Field field = fieldRights.get(fieldName);
        if (unfoundThrowException) {
            Assert.notNull(field, "模型'" + getModel().getName() + "'的字段'" + fieldName + "'不存在或没有权限");
        }
        return field;
    }

    public void sendEvent(Event event) {
        if (eventListeners != null) {
            eventListeners.forEach(listener -> listener.onEvent(event));
        }
    }

    public ModelContext createNew(Permission permission, SqlSession sqlSession) {
        return new ModelContext(model, dialect, mybatisHelper, dataManagerGetter, fillers, permission, eventListeners, interceptor.getInterceptors(), sqlSession);
    }
//
//    /**
//     * 递归查询字段路径对应的模型，并得到模型上下文
//     *
//     * @param fieldPath                        字段路径
//     * @return 模型和模型上下文Map
//     */
//    public Map<String, ModelContext> getModelContexts(String fieldPath, boolean throwException) {
//        int index = fieldPath.indexOf(".");
//        String f1;
//        String f2;
//        if (index > 0) {
//            f1 = fieldPath.substring(0, index);
//            f2 = fieldPath.substring(index + 1);
//        } else {
//            f1 = fieldPath;
//            f2 = null;
//        }
//        Field field = getPermissionedField(f1, throwException);
//        if (field == null) {
//            return Collections.emptyMap();
//        }
//        if (!(field instanceof ToOneField || field instanceof ToManyField)) {
//            if (throwException) {
//                throw new JoinFieldException("40039584", "join字段'" + fieldPath + "'不是 关联类型字段");
//            } else {
//                return Collections.emptyMap();
//            }
//        }
//
//        Map<String, ModelContext> result = new HashMap<>();
//
//        String refModelName = ((RefModel) field).getTargetModel();
//        ModelContext refModelContext = dataManagerGetter.getDataManager(refModelName, this.sqlSession).getModelContext();
//        result.put(f1, refModelContext);
//
//        if (f2 != null) {
//            Map<String, ModelContext> child = refModelContext.getModelContexts(f2, throwException);
//            child.forEach((k, v) -> result.put(f1 + "." + k, v)
//            );
//        }
//
//        return result;
//    }


}
