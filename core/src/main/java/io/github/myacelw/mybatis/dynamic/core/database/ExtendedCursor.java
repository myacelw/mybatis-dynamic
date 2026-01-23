package io.github.myacelw.mybatis.dynamic.core.database;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;

/**
 * 扩展游标接口，添加获取新的SqlSession方法
 *
 * @author liuwei
 */
public interface ExtendedCursor<T> extends Cursor<T> {

    /**
     * 获取新的SqlSession
     *
     * @return SqlSession
     */
    SqlSession getSqlSession();

}
