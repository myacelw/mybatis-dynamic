package io.github.myacelw.mybatis.dynamic.core.database.impl;

import io.github.myacelw.mybatis.dynamic.core.Database;
import io.github.myacelw.mybatis.dynamic.core.TableServiceBuildUtil;
import io.github.myacelw.mybatis.dynamic.core.database.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MybatisHelperBatchUpdatesTest {

    @Test
    void testBatchUpdates() {
        TableManagerImpl tableService = TableServiceBuildUtil.createTableService(Database.H2);
        MybatisHelper mybatisHelper = tableService.getSqlHelper();
        // Use a separate session for setup/verify to avoid interfering with the batch session internal to batchUpdates
        SqlSession sqlSession = mybatisHelper.getSqlSessionFactory().openSession(true);

        // 1. Create a simple table
        String createTableSql = "CREATE TABLE test_batch_update (id INT PRIMARY KEY, name VARCHAR(255), age INT)";
        mybatisHelper.update(sqlSession, createTableSql, null);

        // 2. Insert some data
        mybatisHelper.insert(sqlSession, "INSERT INTO test_batch_update (id, name, age) VALUES (1, 'Alice', 20)", null);
        mybatisHelper.insert(sqlSession, "INSERT INTO test_batch_update (id, name, age) VALUES (2, 'Bob', 25)", null);
        mybatisHelper.insert(sqlSession, "INSERT INTO test_batch_update (id, name, age) VALUES (3, 'Charlie', 30)", null);

        // 3. Prepare heterogeneous batch updates
        // Update 1: Set name='Alice Updated' where id=1
        String sql1 = "UPDATE test_batch_update SET name = #{name} WHERE id = #{id}";
        Map<String, Object> ctx1 = new HashMap<>();
        ctx1.put("id", 1);
        ctx1.put("name", "Alice Updated");

        // Update 2: Set age=26 where id=2
        String sql2 = "UPDATE test_batch_update SET age = #{age} WHERE id = #{id}";
        Map<String, Object> ctx2 = new HashMap<>();
        ctx2.put("id", 2);
        ctx2.put("age", 26);
        
        // Update 3: Set name='Charlie Updated' and age=31 where id=3
        String sql3 = "UPDATE test_batch_update SET name = #{name}, age = #{age} WHERE id = #{id}";
        Map<String, Object> ctx3 = new HashMap<>();
        ctx3.put("id", 3);
        ctx3.put("name", "Charlie Updated");
        ctx3.put("age", 31);

        List<MybatisHelper.BatchItem> items = Arrays.asList(
            new MybatisHelper.BatchItem(sql1, ctx1),
            new MybatisHelper.BatchItem(sql2, ctx2),
            new MybatisHelper.BatchItem(sql3, ctx3)
        );

        // 4. Execute batchUpdates
        // Note: batchUpdates opens its own BATCH session
        boolean success = mybatisHelper.batchUpdates(items, 2); // Batch size 2 to test flushing
        assertTrue(success);

        // 5. Verify results
        Map<String, Object> result1 = mybatisHelper.queryOne(sqlSession, "SELECT * FROM test_batch_update WHERE id = 1", null, Map.class);
        assertEquals("Alice Updated", result1.get("NAME"));
        assertEquals(20, result1.get("AGE")); // Age should remain 20

        Map<String, Object> result2 = mybatisHelper.queryOne(sqlSession, "SELECT * FROM test_batch_update WHERE id = 2", null, Map.class);
        assertEquals("Bob", result2.get("NAME")); // Name should remain Bob
        assertEquals(26, result2.get("AGE"));

        Map<String, Object> result3 = mybatisHelper.queryOne(sqlSession, "SELECT * FROM test_batch_update WHERE id = 3", null, Map.class);
        assertEquals("Charlie Updated", result3.get("NAME"));
        assertEquals(31, result3.get("AGE"));
        
        sqlSession.close();
    }
}