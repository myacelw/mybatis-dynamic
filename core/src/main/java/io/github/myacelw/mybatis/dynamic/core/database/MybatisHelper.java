package io.github.myacelw.mybatis.dynamic.core.database;

import io.github.myacelw.mybatis.dynamic.core.metadata.enums.KeyGeneratorMode;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.SelectColumn;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

/**
 * Mybatis语句执行帮助类
 *
 * @author liuwei
 */
public interface MybatisHelper {

    <T> List<T> queryList(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, Class<T> resultClass);

    /**
     * 查询返回指定的结果类型
     *
     * @param sql
     * @param context
     * @return
     */
    default List<Map<String, Object>> queryList(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns) {
        return (List) queryList(sqlSession, sql, context, columns, Map.class);
    }

    default <T> List<T> queryList(SqlSession sqlSession, String sql, Object context, Class<T> resultClass) {
        return queryList(sqlSession, sql, context, null, resultClass);
    }

    <T> T queryOne(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, Class<T> javaType);

    /**
     * 查询返回指定的结果类型
     *
     * @param sql
     * @return
     */
    default Map<String, Object> queryOne(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns) {
        return queryOne(sqlSession, sql, context, columns, Map.class);
    }

    default <T> T queryOne(SqlSession sqlSession, String sql, Object context, Class<T> javaType) {
        return queryOne(sqlSession, sql, context, null, javaType);
    }

    <K, T> Map<K, T> queryMap(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, Class<T> javaType, String key);

    /**
     * 查询返回指定的结果类型
     *
     * @param sql
     * @param context
     * @return
     */
    default <K> Map<K, Map<String, Object>> queryMap(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, String key) {
        return (Map) queryMap(sqlSession, sql, context, columns, Map.class, key);
    }


    <T> void queryCallBack(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, Class<T> resultClass, ResultHandler<T> handler);

    default void queryCallBack(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, ResultHandler<Map> handler) {
        queryCallBack(sqlSession, sql, context, columns, Map.class, handler);
    }

    default <T> void queryCallBack(SqlSession sqlSession, String sql, Object context, Class<T> resultClass, ResultHandler<T> handler) {
        queryCallBack(sqlSession, sql, context, null, resultClass, handler);
    }

    <T> Cursor<T> queryCursor(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns, Class<T> resultClass);

    default <T> Cursor<T> queryCursor(SqlSession sqlSession, String sql, Object context, Class<T> resultClass) {
        return queryCursor(sqlSession, sql, context, null, resultClass);
    }

    default Cursor<Map<String, Object>> queryCursor(SqlSession sqlSession, String sql, Object context, List<SelectColumn> columns) {
        return (Cursor) queryCursor(sqlSession, sql, context, columns, Map.class);
    }

    default int insert(SqlSession sqlSession, String sql, Object context) {
        return insert(sqlSession, sql, context, null, null, null);
    }

    int insert(SqlSession sqlSession, String sql, Object context, KeyGeneratorMode keyGeneratorMode, String keyGeneratorColumn, String keyGeneratorSequenceName);

    boolean batchInsert(String sql, List<Object> contexts, int batchSize, KeyGeneratorMode keyGeneratorMode, String keyGeneratorColumn, String keyGeneratorSequenceName);

    boolean batchUpdate(String sql, List<Object> contexts, int batchSize);

    int update(SqlSession sqlSession, String sql, Object context);

    int delete(SqlSession sqlSession, String sql, Object context);

    org.apache.ibatis.session.SqlSessionFactory getSqlSessionFactory();
}
