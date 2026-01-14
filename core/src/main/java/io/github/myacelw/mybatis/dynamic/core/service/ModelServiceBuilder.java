package io.github.myacelw.mybatis.dynamic.core.service;

import io.github.myacelw.mybatis.dynamic.core.database.MybatisHelper;
import io.github.myacelw.mybatis.dynamic.core.database.TableManager;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.database.impl.DataBaseMetaDataHelperImpl;
import io.github.myacelw.mybatis.dynamic.core.database.impl.MybatisHelperImpl;
import io.github.myacelw.mybatis.dynamic.core.database.impl.TableManagerImpl;
import io.github.myacelw.mybatis.dynamic.core.event.EventListener;
import io.github.myacelw.mybatis.dynamic.core.service.filler.Filler;
import io.github.myacelw.mybatis.dynamic.core.service.handler.ColumnTypeHandler;
import io.github.myacelw.mybatis.dynamic.core.service.impl.Class2ModelTransferImpl;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelServiceImpl;
import io.github.myacelw.mybatis.dynamic.core.service.impl.ModelToTableConverterImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型服务构建器
 *
 * @author liuwei
 */
@Slf4j
public class ModelServiceBuilder {

    final SqlSessionFactory sqlSessionFactory;

    DataBaseDialect dialect;
    Integer rowLimit;
    Integer timeoutSeconds;
    List<Filler> fillers;
    List<DataChangeInterceptor> interceptors;
    PermissionGetter permissionGetter;
    List<EventListener> eventListeners;

    String tablePrefix;
    String seqPrefix;
    String indexPrefix;
    List<ColumnTypeHandler> columnTypeHandlers;

    /**
     * 注释注解类型，例如填写为 io.swagger.v3.oas.annotations.media.Schema
     */
    Class<? extends Annotation> commentAnnotationClass;

    /**
     * 注释注解字段名称，例如填写为 title
     */
    String commentAnnotationFieldName;

    /**
     * 是否禁用注释 alter 语句
     */
    Boolean disableAlterComment;

    public static ModelServiceBuilder builder(SqlSessionFactory sqlSessionFactory) {
        return new ModelServiceBuilder(sqlSessionFactory);
    }

    public ModelServiceBuilder(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public ModelServiceBuilder dialect(DataBaseDialect dialect) {
        this.dialect = dialect;
        return this;
    }

    public ModelServiceBuilder rowLimit(Integer rowLimit) {
        this.rowLimit = rowLimit;
        return this;
    }

    public ModelServiceBuilder timeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public ModelServiceBuilder fillers(List<Filler> fillers) {
        this.fillers = fillers;
        return this;
    }

    public ModelServiceBuilder interceptors(List<DataChangeInterceptor> interceptors) {
        this.interceptors = interceptors;
        return this;
    }

    public ModelServiceBuilder eventListeners(List<EventListener> eventListeners) {
        this.eventListeners = eventListeners;
        return this;
    }

    public ModelServiceBuilder permissionGetter(PermissionGetter permissionGetter) {
        this.permissionGetter = permissionGetter;
        return this;
    }

    public ModelServiceBuilder disableAlterComment(Boolean disableAlterComment) {
        this.disableAlterComment = disableAlterComment;
        return this;
    }


    public ModelServiceBuilder tablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
        return this;
    }

    public ModelServiceBuilder seqPrefix(String seqPrefix) {
        this.seqPrefix = seqPrefix;
        return this;
    }

    public ModelServiceBuilder indexPrefix(String indexPrefix) {
        this.indexPrefix = indexPrefix;
        return this;
    }

    public ModelServiceBuilder columnTypeHandlers(List<ColumnTypeHandler> columnTypeHandlers) {
        this.columnTypeHandlers = columnTypeHandlers;
        return this;
    }

    public ModelServiceBuilder commentAnnotationClass(Class<? extends Annotation> commentAnnotationClass) {
        this.commentAnnotationClass = commentAnnotationClass;
        return this;
    }

    public ModelServiceBuilder commentAnnotationFieldName(String commentAnnotationFieldName) {
        this.commentAnnotationFieldName = commentAnnotationFieldName;
        return this;
    }

    public ModelServiceImpl build() {
        DataBaseMetaDataHelperImpl dataBaseMetaDataHelper = new DataBaseMetaDataHelperImpl(sqlSessionFactory);
        MybatisHelper mybatisHelper = new MybatisHelperImpl(sqlSessionFactory, rowLimit, timeoutSeconds);
        if (this.dialect == null) {
            String dbProductName = dataBaseMetaDataHelper.getDatabaseProductName();
            for (DataBaseDialect instance : DataBaseDialect.getInstances()) {
                if (instance.getName().equalsIgnoreCase(dbProductName)) {
                    this.dialect = instance;
                    log.info("自动识别数据库类型:{}", instance.getName());
                    break;
                }
            }
            if (this.dialect == null) {
                throw new IllegalArgumentException("未找到支持的数据库类型:" + dbProductName + "，请直接指定数据库方言");
            }
        }

        TableManager tableManager = new TableManagerImpl(dataBaseMetaDataHelper, mybatisHelper, dialect);
        Map<String, Filler> mergedFiller = new HashMap<>();
        for (Filler filler : Filler.DEFAULT_FILLERS) {
            mergedFiller.put(filler.getName(), filler);
        }
        if (this.fillers != null) {
            for (Filler filler : this.fillers) {
                mergedFiller.put(filler.getName(), filler);
            }
        }
        ModelToTableConverterImpl modelToTableConverter = new ModelToTableConverterImpl(dialect, dataBaseMetaDataHelper, columnTypeHandlers);
        modelToTableConverter.setTablePrefix(tablePrefix);
        modelToTableConverter.setSeqPrefix(seqPrefix);
        modelToTableConverter.setIndexPrefix(indexPrefix);

        Class2ModelTransferImpl class2ModelTransfer = new Class2ModelTransferImpl(commentAnnotationClass, commentAnnotationFieldName);

        ModelServiceImpl modelService = new ModelServiceImpl(
                dialect,
                mybatisHelper,
                tableManager,
                modelToTableConverter,
                mergedFiller, class2ModelTransfer
        );

        modelService.setInterceptors(interceptors);
        modelService.setPermissionGetter(permissionGetter);
        modelService.setEventListeners(eventListeners);
        modelService.setDisableAlterComment(disableAlterComment);
        return modelService;
    }
}