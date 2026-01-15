package io.github.myacelw.mybatis.dynamic.core.database.impl;

import io.github.myacelw.mybatis.dynamic.core.annotation.SubTypes;
import io.github.myacelw.mybatis.dynamic.core.database.MybatisHelper;
import io.github.myacelw.mybatis.dynamic.core.ext.ExtBean;
import io.github.myacelw.mybatis.dynamic.core.metadata.enums.KeyGeneratorMode;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.SelectColumn;
import io.github.myacelw.mybatis.dynamic.core.util.BeanUtil;
import io.github.myacelw.mybatis.dynamic.core.util.DataUtil;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import io.github.myacelw.mybatis.dynamic.core.util.StringUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.*;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mybatis语句执行帮助实现类。
 *
 * @author liuwei
 */
@Slf4j
public class MybatisHelperImpl implements MybatisHelper {
    private static final String SCRIPT_TAG_BEGIN = "<script>";
    private static final String SCRIPT_TAG_END = "</script>";
    @Getter
    private final SqlSessionFactory sqlSessionFactory;
    private final Integer rowLimit;
    private final Integer timeoutSeconds;
    private final MapperBuilderAssistant assistant;

    public MybatisHelperImpl(@NonNull SqlSessionFactory sqlSessionFactory, Integer rowLimit, Integer timeoutSeconds) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.rowLimit = rowLimit;
        this.timeoutSeconds = timeoutSeconds;
        String resource = MybatisHelperImpl.class.getName().replace('.', '/') + ".java";
        this.assistant = new MapperBuilderAssistant(sqlSessionFactory.getConfiguration(), resource);
    }

    private <T> T exeSql(SqlSession sqlSession, Function<SqlSession, T> function) {
        if (sqlSession == null) {
            try (SqlSession newSqlSession = sqlSessionFactory.openSession(true)) {
                return function.apply(newSqlSession);
            }
        } else {
            return function.apply(sqlSession);
        }
    }

    @Override
    public <T> List<T> queryList(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, Class<T> resultClass) {
        String msId = dynamicSql(sql, resultClass, columns, SqlCommandType.SELECT, null, null);
        return exeSql(sqlSession, session -> {
            if (rowLimit != null && rowLimit > 0) {
                return session.selectList(msId, context, new RowBounds(0, rowLimit));
            } else {
                return session.selectList(msId, context);
            }
        });
    }

    @Override
    public <K, T> Map<K, T> queryMap(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, Class<T> javaType, String key) {
        String msId = dynamicSql(sql, javaType, columns, SqlCommandType.SELECT, null, null);
        return exeSql(sqlSession, session -> {
            if (rowLimit != null && rowLimit > 0) {
                return session.selectMap(msId, context, key, new RowBounds(0, rowLimit));
            } else {
                return session.selectMap(msId, context, key);
            }
        });
    }

    @Override
    public <T> T queryOne(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, Class<T> javaType) {
        String msId = dynamicSql(sql, javaType, columns, SqlCommandType.SELECT, null, null);
        return exeSql(sqlSession, session -> session.selectOne(msId, context));
    }

    @Override
    public <T> void queryCallBack(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, Class<T> resultClass, ResultHandler<T> handler) {
        String msId = dynamicSql(sql, resultClass, columns, SqlCommandType.SELECT, null, null);
        // 回调方法 不受 rowLimit 控制
        exeSql(sqlSession, session -> {
            session.select(msId, context, handler);
            return null;
        });
    }

    @Override
    public <T> Cursor<T> queryCursor(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, Class<T> resultClass) {
        String msId = dynamicSql(sql, resultClass, columns, SqlCommandType.SELECT, null, null);

        Function<SqlSession, Cursor<T>> function = session -> {
            if (rowLimit != null && rowLimit > 0) {
                return session.selectCursor(msId, context, new RowBounds(0, rowLimit));
            } else {
                return session.selectCursor(msId, context);
            }
        };
        if (sqlSession != null) {
            return function.apply(sqlSession);
        }

        return new Cursor<T>() {
            final SqlSession newSqlSession = sqlSessionFactory.openSession(true);
            final Cursor<T> result = function.apply(newSqlSession);

            @Override
            public void close() throws IOException {
                result.close();
                newSqlSession.close();
            }

            @Override
            public boolean isOpen() {
                return result.isOpen();
            }

            @Override
            public boolean isConsumed() {
                return result.isConsumed();
            }

            @Override
            public int getCurrentIndex() {
                return result.getCurrentIndex();
            }

            @Override
            public Iterator<T> iterator() {
                return result.iterator();
            }
        };
    }

    @Override
    public int insert(SqlSession sqlSession, String sql, Object context, KeyGeneratorMode keyGeneratorMode, String keyGeneratorColumn, String keyGeneratorSequenceName) {
        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;

        if (keyGeneratorMode == KeyGeneratorMode.AUTO_INCREMENT) {
            keyGenerator = Jdbc3KeyGenerator.INSTANCE;
        } else if (keyGeneratorMode == KeyGeneratorMode.SEQUENCE) {
            keyGenerator = createSelectKeyGenerator(keyGeneratorSequenceName);
        }

        String msId = dynamicSql(sql, Integer.class, SqlCommandType.INSERT, keyGenerator, keyGeneratorColumn);
        return exeSql(sqlSession, session -> session.insert(msId, context));
    }

    @Override
    public boolean batchInsert(String sql, List<Object> contexts, Integer batchSize, KeyGeneratorMode keyGeneratorMode, String keyGeneratorColumn, String keyGeneratorSequenceName) {
        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;

        if (keyGeneratorMode == KeyGeneratorMode.AUTO_INCREMENT) {
            keyGenerator = Jdbc3KeyGenerator.INSTANCE;
        } else if (keyGeneratorMode == KeyGeneratorMode.SEQUENCE) {
            keyGenerator = createSelectKeyGenerator(keyGeneratorSequenceName);
        }

        String msId = dynamicSql(sql, Integer.class, SqlCommandType.INSERT, keyGenerator, keyGeneratorColumn);
        return executeBatch(contexts, batchSize, (session, entity) -> session.insert(msId, entity));
    }

    @Override
    public boolean batchUpdate(String sql, List<Object> contexts, Integer batchSize) {
        String msId = dynamicSql(sql, Integer.class, SqlCommandType.UPDATE, null, null);
        return executeBatch(contexts, batchSize, (session, entity) -> session.update(msId, entity));
    }

    /**
     * update语句执行
     *
     * @param sql
     * @param context
     * @return int
     * @date 2022/10/27
     **/
    @Override
    public int update(SqlSession sqlSession, String sql, Object context) {
        String msId = dynamicSql(sql, Integer.class, SqlCommandType.UPDATE, null, null);
        return exeSql(sqlSession, session -> session.update(msId, context));
    }

    /**
     * delete语句执行
     *
     * @param sql
     * @param context
     * @return int
     * @date 2022/10/27
     **/
    @Override
    public int delete(SqlSession sqlSession, String sql, Object context) {
        String msId = dynamicSql(sql, Integer.class, SqlCommandType.DELETE, null, null);
        return exeSql(sqlSession, session -> session.delete(msId, context));
    }

    private String dynamicSql(String mybatisSQL, Class<?> resultClass, SqlCommandType type, KeyGenerator keyGenerator, String keyProperty) {
        return this.dynamicSql(mybatisSQL, resultClass, null, type, keyGenerator, keyProperty);
    }

    /**
     * sql缓存及处理
     *
     * @param mybatisSQL
     * @param resultClass
     * @param type
     * @return java.lang.String
     * @date 2022/10/27
     **/
    private String dynamicSql(String mybatisSQL, Class<?> resultClass, List<SelectColumn> columns, SqlCommandType type, KeyGenerator keyGenerator, String keyProperty) {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        LanguageDriver languageDriver = configuration.getDefaultScriptingLanguageInstance();

        String resultMapId = getResultMap(resultClass, columns);
        String msId = getMappedStatementId(mybatisSQL, type, resultMapId, keyGenerator, keyProperty);

        if (configuration.hasStatement(msId, false)) {
            return msId;
        }

        String expressionSQL;
        if (mybatisSQL.contains("<") && mybatisSQL.contains(">")) {
            expressionSQL = SCRIPT_TAG_BEGIN + sqlPreprocessing(mybatisSQL) + SCRIPT_TAG_END;
        } else {
            expressionSQL = mybatisSQL;
        }

        SqlSource sqlSource = languageDriver.createSqlSource(configuration, expressionSQL, null);
        MappedStatement.Builder msb = new MappedStatement.Builder(configuration, msId, sqlSource, type);

        ResultMap resultMap = configuration.getResultMap(resultMapId);
        msb.resultMaps(Collections.singletonList(resultMap));

        if (timeoutSeconds != null && timeoutSeconds > 0) {
            msb.timeout(timeoutSeconds);
        }
        if (keyGenerator != null) {
            msb.keyGenerator(keyGenerator);
        }
        if (keyProperty != null) {
            msb.keyProperty(keyProperty);
        }

        MappedStatement ms = msb.build();
        if (log.isTraceEnabled()) {
            log.trace(ms.getSqlSource().toString());
        }

        synchronized (configuration) {
            if (!configuration.hasStatement(msId, false)) {
                configuration.addMappedStatement(ms);
            }
        }
        return msId;
    }

    private String getMappedStatementId(String mybatisSQL, SqlCommandType type, String resultMapId, KeyGenerator keyGenerator, String keyProperty) {
        String msId = "DynamicModel." + type.toString() + "_" + mybatisSQL.hashCode() + "_" + resultMapId + (keyGenerator != null ? "_" + keyGenerator.getClass().getSimpleName() + "_" + keyProperty : "");
        if (mybatisSQL.contains("/* IgnoreTenantLine */")) {
            msId = msId + "-IgnoreTenantLine";
        }
        return msId;
    }

    private String getResultMap(Class<?> resultClass, List<SelectColumn> columns) {
        Configuration configuration = sqlSessionFactory.getConfiguration();

        if (columns == null) {
            columns = Collections.emptyList();
        } else {
            columns = columns.stream().sorted().collect(Collectors.toList());
        }

        String id = getResultMapId(resultClass, columns);
        if (configuration.hasResultMap(id)) {
            return id;
        }
        if (resultClass != null && !DataUtil.isBasicType(resultClass)) {
            SubTypes anno = resultClass.getDeclaredAnnotation(SubTypes.class);
            if (anno != null && !ObjectUtil.isEmpty(anno.subTypes())) {
                return createDiscriminatorResultMap(id, resultClass, columns, anno);
            }
        }
        return doConvertToResultMap(resultClass, columns);
    }

    private String createDiscriminatorResultMap(String id, Class<?> resultClass, List<SelectColumn> columns, SubTypes anno) {
        Configuration configuration = sqlSessionFactory.getConfiguration();

        Map<String, String> discriminatorMap = new HashMap<>();
        for (SubTypes.SubType subType : anno.subTypes()) {
            Class<?> type = subType.value();
            String name = StringUtil.hasText(subType.name()) ? subType.name() : type.getSimpleName();
            String subId = doConvertToResultMap(type, columns);
            discriminatorMap.put(name, subId);
        }
        Discriminator discriminator = new Discriminator.Builder(configuration,
                new ResultMapping.Builder(configuration, null, anno.subTypeFieldName(), String.class).build(),
                discriminatorMap).build();

        synchronized (configuration) {
            if (!configuration.hasResultMap(id)) {
                configuration.addResultMap(new ResultMap.Builder(configuration, id, resultClass, new ArrayList<>()).discriminator(discriminator).build());
            }
        }

        return id;
    }

    private static String getResultMapId(Class<?> resultClass, List<SelectColumn> columns) {
        return "ResultMap-" + (resultClass == null ? "Object" : resultClass.getName()) + (ObjectUtil.isEmpty(columns) ? "" : columns.hashCode());
    }

    /**
     * 创建resultMap
     */
    protected String doConvertToResultMap(Class<?> resultClass, List<SelectColumn> columns) {
        Configuration configuration = sqlSessionFactory.getConfiguration();

        String id = getResultMapId(resultClass, columns);
        if (configuration.hasResultMap(id)) {
            return id;
        }

        List<ResultMapping> resultMappings = new ArrayList<>();

        PropertyConvertor pc = new PropertyConvertor(resultClass);

        for (SelectColumn column : columns) {
            String propertyName = column.getProperty();
            Class<?> javaType = column.getJavaType();
            TypeHandler<?> typeHandler = column.getTypeHandler();
            JdbcType jdbcType = column.getJdbcType();

            String finalPropertyName = pc.getFinalPropertyName(propertyName);

            if (finalPropertyName != null) {
                ResultMapping.Builder builder = new ResultMapping.Builder(configuration,
                        finalPropertyName,
                        propertyName, // select 中 已经将返回列 AS 为了 propertyName
                        javaType);

                if (typeHandler != null) {
                    builder.typeHandler(typeHandler);
                }
                if (jdbcType != null) {
                    builder.jdbcType(jdbcType);
                }

                if (column.getType() == SelectColumn.Type.ID) {
                    builder.flags(Collections.singletonList(ResultFlag.ID));
                } else if (column.getType() == SelectColumn.Type.COLLECTION) {
                    Class<?> subJavaType = javaType == null ? Map.class : javaType;
                    builder.javaType(List.class);
                    builder.column(null);
                    builder.columnPrefix(column.getProperty() + ".");
                    builder.nestedResultMapId(getResultMap(subJavaType, column.getComposites()));
                } else if (column.getType() == SelectColumn.Type.ASSOCIATION) {
                    Class<?> subJavaType = javaType == null ? Map.class : javaType;
                    builder.javaType(subJavaType);
                    builder.column(null);
                    builder.columnPrefix(column.getProperty() + ".");
                    builder.nestedResultMapId(getResultMap(subJavaType, column.getComposites()));
                }
                resultMappings.add(builder.build());
            }
        }
        synchronized (configuration) {
            if (!configuration.hasResultMap(id)) {
                configuration.addResultMap(new ResultMap.Builder(configuration, id, resultClass, resultMappings).build());
            }
        }
        return id;
    }

    private static String sqlPreprocessing(String sql) {
        if (sql == null || !sql.contains("<")) {
            return sql;
        } else {
            int i = sql.indexOf("<![CDATA[");
            if (i == -1) {
                return sqlEscape(sql);
            } else {
                int j = sql.indexOf("]]>", i + 1);
                if (j < 0) {
                    return sql;
                } else {
                    int k = j + "]]>".length();
                    k = Math.min(k, sql.length());
                    String s1 = sql.substring(0, i);
                    String s2 = sql.substring(i, k);
                    String s3 = sql.substring(k);
                    return sqlPreprocessing(s1) + s2 + sqlPreprocessing(s3);
                }
            }
        }
    }

    private static String sqlEscape(String sql) {
        return sql.replace("< ", "&lt; ").replace("<=", "&lt;=").replace("<>", "&lt;>");
    }

    /**
     * 真实属性名转换器
     */
    static class PropertyConvertor {

        Class<?> resultClass;
        // 是否基本类型
        boolean basicType;
        // 是否ExtBean
        boolean extBean;
        Set<String> fields;

        PropertyConvertor(Class<?> resultClass) {
            this.resultClass = resultClass;
            basicType = resultClass == null || DataUtil.isBasicType(resultClass);
            extBean = resultClass != null && ExtBean.class.isAssignableFrom(resultClass);

            if (!basicType) {
                fields = getFields(resultClass);
            }
        }

        private Set<String> getFields(Class<?> entityClass) {
            Set<String> fields = new HashSet<>();
            PropertyDescriptor[] pds = BeanUtil.getPropertyDescriptors(entityClass);
            for (PropertyDescriptor pd : pds) {
                if (pd.getWriteMethod() != null) {
                    fields.add(pd.getName());
                }
            }
            return fields;
        }

        public String getFinalPropertyName(String propertyName) {
            if (basicType) {
                //基本类型 不判断是否含有对应属性名，直接返回
                return propertyName;
            }

            String prop0 = propertyName;
            if (propertyName.contains(".")) {
                prop0 = propertyName.split("\\.")[0];
            }

            if (fields.contains(prop0)) {
                return propertyName;
            } else if (extBean) {
                //ExtMap 如果实体本身没有该属性则保存到ext map中
                return ExtBean.NAME + "." + propertyName;
            } else {
                return null;
            }
        }
    }

    private <E> boolean executeBatch(List<Object> contexts, int batchSize, BiConsumer<SqlSession, E> consumer) {
        try (SqlSession batchSqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            int size = contexts.size();
            for (int i = 0; i < size; i++) {
                @SuppressWarnings("unchecked")
                E entity = (E) contexts.get(i);
                consumer.accept(batchSqlSession, entity);
                if ((i + 1) % batchSize == 0 || i == size - 1) {
                    batchSqlSession.flushStatements();
                }
            }
            batchSqlSession.commit();
            return true;
        }
    }

    private KeyGenerator createSelectKeyGenerator(String keyGeneratorSequenceName) {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        LanguageDriver languageDriver = configuration.getDefaultScriptingLanguageInstance();

        String id = keyGeneratorSequenceName + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        boolean executeBefore = true;
        StatementType statementType = StatementType.PREPARED;
        String sql = "select " + keyGeneratorSequenceName + ".nextval from dual";
        Class<?> resultTypeClass = Integer.class;
        String databaseId = null;

        SqlSource sqlSource = buildSqlSourceFromStrings(new String[]{sql}, null);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;

        assistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType, null, null, null,
                null, null, resultTypeClass, null, false, false, false, NoKeyGenerator.INSTANCE,
                null, null, databaseId, languageDriver, null, false);

        id = assistant.applyCurrentNamespace(id, false);

        MappedStatement keyStatement = configuration.getMappedStatement(id, false);
        SelectKeyGenerator answer = new SelectKeyGenerator(keyStatement, executeBefore);
        configuration.addKeyGenerator(id, answer);
        return answer;
    }

    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass) {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        LanguageDriver languageDriver = configuration.getDefaultScriptingLanguageInstance();

        return languageDriver.createSqlSource(configuration, String.join(" ", strings).trim(), parameterTypeClass);
    }

}
