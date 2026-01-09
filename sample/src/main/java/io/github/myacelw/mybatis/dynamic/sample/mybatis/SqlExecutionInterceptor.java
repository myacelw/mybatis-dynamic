package io.github.myacelw.mybatis.dynamic.sample.mybatis;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL语句拦截器
 *
 * @author liuwei
 */
@Component
@Intercepts({
        // 拦截 UPDATE 操作
        @Signature(
                type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}
        ),

        // 拦截 普通查询 (4 参数)
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        ),

        // 拦截 完整查询 (6 参数，包含二级缓存和 BoundSql)
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
        ),

        // 拦截 流式查询 (queryCursor)
        @Signature(
                type = Executor.class,
                method = "queryCursor",
                args = {MappedStatement.class, Object.class, RowBounds.class}
        )
})
public class SqlExecutionInterceptor implements Interceptor {

    /**
     * SQL 执行监听器线程本地变量
     */
    public final static ThreadLocal<SqlExecListener> sqlExecListenerThreadLocal = new ThreadLocal<>();

    /**
     * 拦截方法的核心逻辑
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取 SQL 执行监听器
        SqlExecListener sqlExecListener = sqlExecListenerThreadLocal.get();

        if (sqlExecListener != null) {
            MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
            Object parameter = invocation.getArgs()[1];
            BoundSql boundSql = invocation.getArgs().length >= 5 ? (BoundSql) invocation.getArgs()[5] : ms.getBoundSql(parameter);

            // 调用监听器的 onBeforeExecute 方法
            sqlExecListener.onBeforeExecute(boundSql.getSql(), getParameterValues(boundSql, ms.getConfiguration()));
        }

        // 继续执行原方法
        return invocation.proceed();
    }

    /**
     * 从 BoundSql 中提取参数值列表，并能处理嵌套属性（如 a.b.c）。
     * * @param boundSql 包含参数映射信息的对象
     * @param configuration MyBatis 配置对象，用于创建 MetaObject
     * @return 包含所有参数值的列表，与 SQL 中的 ? 顺序一致
     */
    public static List<Object> getParameterValues(BoundSql boundSql, Configuration configuration) {

        List<Object> parameterValues = new ArrayList<>();
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        if (parameterMappings != null && !parameterMappings.isEmpty()) {

            // 默认创建 MetaObject，MyBatis 的MetaObject能够处理：
            // 1. POJO 的普通属性
            // 2. POJO 的嵌套属性（如 a.b.c）
            // 3. Map 的键值
            // 4. List 或数组的索引（如 list[0]）
            MetaObject metaObject = configuration.newMetaObject(parameterObject);

            for (ParameterMapping parameterMapping : parameterMappings) {

                String propertyName = parameterMapping.getProperty();
                Object value;

                if (boundSql.hasAdditionalParameter(propertyName)) {
                    // 情况 1: 额外参数 (如 RowBounds, _databaseId, 或拦截器添加的参数)
                    value = boundSql.getAdditionalParameter(propertyName);

                } else if (parameterObject == null) {
                    // 情况 2: 参数对象是 null
                    value = null;

                } else if (parameterObject instanceof Number || parameterObject instanceof String
                        || parameterObject.getClass().isPrimitive()) {
                    // 情况 3: 简单类型/基本类型 (Integer, String, int 等)。
                    // 它们在 ParameterMapping 中的 propertyName 通常是 "value"
                    value = parameterObject;

                } else if (configuration.getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass())) {
                    // 情况 4: 带有自定义 TypeHandler 的参数对象，通常也直接使用对象本身
                    value = parameterObject;
                } else {
                    // 情况 5: POJO (包括嵌套属性), Map, List/Array
                    // MetaObject.getValue() 可以安全地处理 "a.b.c" 这样的嵌套路径
                    if (metaObject.hasGetter(propertyName)) {
                        value = metaObject.getValue(propertyName);
                    } else {
                        // 如果 MetaObject 找不到 Getter，通常发生在特殊情况下，视为 null 或保持原样
                        value = null;
                    }
                }

                parameterValues.add(value);
            }
        }

        return parameterValues;
    }


}
