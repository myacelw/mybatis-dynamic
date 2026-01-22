package io.github.myacelw.mybatis.dynamic.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.*;

import io.github.myacelw.mybatis.dynamic.core.database.impl.DataBaseMetaDataHelperImpl;
import io.github.myacelw.mybatis.dynamic.core.database.impl.MybatisHelperImpl;
import io.github.myacelw.mybatis.dynamic.core.database.impl.TableManagerImpl;
import lombok.SneakyThrows;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;

import javax.sql.DataSource;

public class TableServiceBuildUtil {

    public static SqlSessionFactory createSqlSessionFactory(Database database) {
        return createSqlSessionFactory(database, "test");
    }

    public static SqlSessionFactory createSqlSessionFactory(Database database, String h2Name) {
        DataSource dataSource;
        if (database == Database.MYSQL) {
            dataSource = dataSourceForMysql();
        } else if (database == Database.POSTGRESQL) {
            dataSource = dataSourceForPostgresql();
        } else if (database == Database.OCEANBASE) {
            dataSource = dataSourceForOceanBase();
        } else {
            dataSource = dataSourceForH2(h2Name);
        }
        return sqlSessionFactory(dataSource);
    }


    public static TableManagerImpl createTableService(Database database) {
        return createTableService(database, "test");
    }

    public static TableManagerImpl createTableService(Database database, String h2Name) {
        DataSource dataSource;
        DataBaseDialect dialect;
        if (database == Database.MYSQL) {
            dataSource = dataSourceForMysql();
            dialect = new MysqlDataBaseDialect();
        } else if (database == Database.POSTGRESQL) {
            dataSource = dataSourceForPostgresql();
            dialect = new PostgresqlDataBaseDialect();
        } else if (database == Database.OCEANBASE) {
            dataSource = dataSourceForOceanBase();
            dialect = new OceanBaseDataBaseDialect();
        } else {
            dataSource = dataSourceForH2(h2Name);
            dialect = new H2DataBaseDialect();
        }
        DataBaseMetaDataHelperImpl metaDataHelper = new DataBaseMetaDataHelperImpl(sqlSessionFactory(dataSource));
        return new TableManagerImpl(metaDataHelper, new MybatisHelperImpl(sqlSessionFactory(dataSource), 0, 120), dialect);
    }

    private static DataSource dataSourceForOceanBase() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:oceanbase://127.0.0.1:2881/teest?enableQueryTimeouts=false&characterEncoding=utf8&useSSL=false&allowMultiQueries=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
        config.setDriverClassName("com.oceanbase.jdbc.Driver");
        config.setUsername("root@test");
        config.setPassword("123456");
        return new HikariDataSource(config);
    }

    @SneakyThrows
    private static SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        Configuration configuration = new Configuration();
//        configuration.setCallSettersOnNulls(true);
        configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), dataSource));
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private static DataSource dataSourceForH2(String h2Name) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:" + h2Name + ";MODE=MySQL");
        config.setUsername("sa");
        return new HikariDataSource(config);
    }

    private static DataSource dataSourceForMysql() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test");
        config.setUsername("test");
        config.setPassword("test");
        return new HikariDataSource(config);
    }

    private static DataSource dataSourceForPostgresql() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/test");
        config.setUsername("test");
        config.setPassword("test");
        return new HikariDataSource(config);
    }

}
