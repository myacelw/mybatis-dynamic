package io.github.myacelw.mybatis.dynamic.core.database.impl;

import io.github.myacelw.mybatis.dynamic.core.Database;
import io.github.myacelw.mybatis.dynamic.core.TableServiceBuildUtil;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Column;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Index;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataBaseMetaDataHelperImplTest {

    static DataBaseMetaDataHelperImpl metaDataHelper;
    static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    static void setup() throws Exception {
        sqlSessionFactory = TableServiceBuildUtil.createSqlSessionFactory(Database.H2, "DataBaseMetaDataHelperImplTest");
        metaDataHelper = new DataBaseMetaDataHelperImpl(sqlSessionFactory);

        try (Connection connection = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE test_table (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL, description TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("COMMENT ON TABLE test_table IS 'Test table comment'");
            statement.execute("COMMENT ON COLUMN test_table.name IS 'Name column comment'");
            statement.execute("CREATE INDEX idx_test_name ON test_table(name)");
            statement.execute("CREATE UNIQUE INDEX uk_test_id_name ON test_table(id, name)");
        }
    }

    @Test
    void getDatabaseProductName() {
        String productName = metaDataHelper.getDatabaseProductName();
        assertEquals("H2", productName);
    }

    @Test
    void getIdentifierQuoteString() {
        String quoteString = metaDataHelper.getIdentifierQuoteString();
        assertEquals("\"", quoteString);
    }
}
