package io.github.myacelw.mybatis.dynamic.core;

import io.github.myacelw.mybatis.dynamic.core.database.dialect.*;

import io.github.myacelw.mybatis.dynamic.core.database.impl.MybatisHelperImpl;
import io.github.myacelw.mybatis.dynamic.core.database.impl.TableManagerImpl;
import lombok.SneakyThrows;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import javax.sql.DataSource;

public class TableServiceBuildUtil {

    public static SqlSessionFactory createSqlSessionFactory(Database database) {
        DataSource dataSource;
        if (database == Database.MYSQL) {
            dataSource = dataSourceForMysql();
        } else if (database == Database.POSTGRESQL) {
            dataSource = dataSourceForPostgresql();
        } else if (database == Database.OCEANBASE) {
            dataSource = dataSourceForOceanBase();
        } else {
            dataSource = dataSourceForH2();
        }
        return sqlSessionFactory(dataSource);
    }


    public static TableManagerImpl createTableService(Database database) {
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
            dataSource = dataSourceForH2();
            dialect = new H2DataBaseDialect();
        }
        return new TableManagerImpl(new MybatisHelperImpl(sqlSessionFactory(dataSource), 0, 120), dialect);
    }

    private static DataSource dataSourceForOceanBase() {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:oceanbase://127.0.0.1:2881/teest?enableQueryTimeouts=false&characterEncoding=utf8&useSSL=false&allowMultiQueries=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
        properties.setDriverClassName("com.oceanbase.jdbc.Driver");
        properties.setUsername("root@test");
        properties.setPassword("123456");
        return properties.initializeDataSourceBuilder().build();
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

    private static DataSource dataSourceForH2() {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:h2:mem:test;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DATABASE_TO_UPPER=FALSE");
        properties.setUsername("sa");
        return properties.initializeDataSourceBuilder().build();
    }

    private static DataSource dataSourceForMysql() {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:mysql://127.0.0.1:3306/test");
        properties.setUsername("test");
        properties.setPassword("test");
        return properties.initializeDataSourceBuilder().build();
    }

    private static DataSource dataSourceForPostgresql() {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:postgresql://127.0.0.1:5432/test");
        properties.setUsername("test");
        properties.setPassword("test");
        return properties.initializeDataSourceBuilder().build();
    }

}
