package io.github.myacelw.mybatis.dynamic.core.service.execution;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.github.myacelw.mybatis.dynamic.core.Database;
import io.github.myacelw.mybatis.dynamic.core.TableServiceBuildUtil;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.ModelDataLoader;
import io.github.myacelw.mybatis.dynamic.core.service.ModelService;
import io.github.myacelw.mybatis.dynamic.core.service.ModelServiceBuilder;
import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractCreatorFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractModifierFiller;
import io.github.myacelw.mybatis.dynamic.core.service.filler.Filler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class BaseExecutionTest {

    protected SqlSessionFactory sqlSessionFactory;
    protected ModelService modelService;
    protected static String currentUser = "admin";
    protected ModelDataLoader modelDataLoader;

    protected SqlSession sqlSession;
    protected Map<String, List<Map<String, Object>>> initDataMap;

    @BeforeEach
    void setUp() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger("io.github.myacelw.mybatis.dynamic");
        rootLogger.setLevel(Level.DEBUG);

        // Use a unique H2 database name per test instance to avoid interference
        String h2Name = this.getClass().getSimpleName() + "_" + UUID.randomUUID().toString().substring(0, 8);
        this.sqlSessionFactory = TableServiceBuildUtil.createSqlSessionFactory(Database.H2, h2Name);

        List<Filler> fillers = new ArrayList<>();
        fillers.add(new AbstractCreatorFiller() {
            @Override
            protected String getCurrentUser() {
                return currentUser;
            }
        });

        fillers.add(new AbstractModifierFiller() {
            @Override
            protected String getCurrentUser() {
                return currentUser;
            }
        });

        this.modelService = new ModelServiceBuilder(sqlSessionFactory).fillers(fillers).tablePrefix("d_").build();
        this.modelDataLoader = new ModelDataLoader(modelService);
        this.modelDataLoader.setIdType(String.class);
        this.modelDataLoader.updateAndRegister("classpath:models.json");

        this.initDataMap = modelDataLoader.initModelData(sqlSessionFactory, "classpath:data.json");
        this.sqlSession = sqlSessionFactory.openSession();
    }

    @AfterEach
    void tearDown() {
        if (sqlSession != null) {
            sqlSession.close();
        }
        // Truncate all tables to ensure clean state for next test
        try (SqlSession session = sqlSessionFactory.openSession()) {
            for (Model model : modelService.getAllRegisteredModels()) {
                try {
                    String sql = "TRUNCATE TABLE " + model.getSchemaAndTableName();
                    session.update("io.github.myacelw.mybatis.dynamic.core.database.MybatisHelper.update", Collections.singletonMap("sql", sql));
                } catch (Exception e) {
                    // Ignore if table doesn't exist or other issues
                }
            }
            session.commit();
        }
    }

    protected <ID> DataManager<ID> getDataManager(String modelName) {
        return modelService.getDataManager(modelName, sqlSession);
    }
}
