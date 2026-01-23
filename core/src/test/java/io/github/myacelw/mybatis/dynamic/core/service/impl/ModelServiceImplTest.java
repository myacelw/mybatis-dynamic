package io.github.myacelw.mybatis.dynamic.core.service.impl;

import io.github.myacelw.mybatis.dynamic.core.database.MybatisHelper;
import io.github.myacelw.mybatis.dynamic.core.database.TableManager;
import io.github.myacelw.mybatis.dynamic.core.database.dialect.DataBaseDialect;
import io.github.myacelw.mybatis.dynamic.core.event.EventListener;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.TableDefine;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.table.Table;
import io.github.myacelw.mybatis.dynamic.core.metadata.vo.DDLPlan;
import io.github.myacelw.mybatis.dynamic.core.service.Class2ModelTransfer;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.ModelToTableConverter;
import io.github.myacelw.mybatis.dynamic.core.service.PermissionGetter;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelServiceImplTest {

    private DataBaseDialect dialect;
    private MybatisHelper mybatisHelper;
    private TableManager tableManager;
    private ModelToTableConverter modelToTableConverter;
    private Class2ModelTransfer class2ModelTransfer;
    private ModelServiceImpl modelService;

    @BeforeEach
    void setUp() {
        dialect = mock(DataBaseDialect.class);
        mybatisHelper = mock(MybatisHelper.class);
        tableManager = mock(TableManager.class);
        modelToTableConverter = mock(ModelToTableConverter.class);
        class2ModelTransfer = mock(Class2ModelTransfer.class);
        
        modelService = new ModelServiceImpl(dialect, mybatisHelper, tableManager, modelToTableConverter, new HashMap<>(), class2ModelTransfer);
    }

    private Model createValidModel(String name) {
        Model model = new Model();
        model.setName(name);
        model.setTableDefine(new TableDefine());
        BasicField field = new BasicField();
        field.setName("id");
        field.setJavaClass(String.class);
        model.setFields(new ArrayList<>(Collections.singletonList(field)));
        return model;
    }

    @Test
    void register_AddsToInternalMap() {
        Model model = createValidModel("TestModel");

        modelService.register(model);

        assertTrue(modelService.isModelExist("TestModel"));
        assertEquals(1, modelService.getAllRegisteredModels().size());
        assertEquals(model, modelService.getAllRegisteredModels().get(0));
    }

    @Test
    void unregister_RemovesFromInternalMap() {
        Model model = createValidModel("TestModel");

        modelService.register(model);
        modelService.unregister("TestModel");

        assertFalse(modelService.isModelExist("TestModel"));
    }

    @Test
    void update_ExecutesDDLPlan() {
        Model model = createValidModel("TestModel");

        Table table = new Table("TEST_TABLE", null);
        when(modelToTableConverter.convertToTable(eq(model), any())).thenReturn(table);
        
        DDLPlan plan = mock(DDLPlan.class);
        when(plan.isEmpty()).thenReturn(false);
        when(tableManager.getUpdatePlan(table)).thenReturn(plan);

        modelService.update(model);

        verify(tableManager).executePlan(plan);
    }

    @Test
    void update_DryRun_DoesNotExecuteDDL() {
        Model model = createValidModel("TestModel");
        modelService.setDryRun(true);

        Table table = new Table("TEST_TABLE", null);
        when(modelToTableConverter.convertToTable(eq(model), any())).thenReturn(table);
        
        DDLPlan plan = mock(DDLPlan.class);
        when(plan.isEmpty()).thenReturn(false);
        when(tableManager.getUpdatePlan(table)).thenReturn(plan);

        modelService.update(model);

        verify(tableManager, never()).executePlan(any());
    }

    @Test
    void delete_DropsTable() {
        Model model = createValidModel("TestModel");

        Table table = new Table("TEST_TABLE", null);
        when(modelToTableConverter.convertToTable(any(), any())).thenReturn(table);

        modelService.delete(model);

        verify(tableManager).dropTable(table);
        assertFalse(modelService.isModelExist("TestModel"));
    }

    @Test
    void getDataManager_WithPermissions() {
        Model model = createValidModel("PermModel");
        modelService.register(model);

        SqlSession sqlSession = mock(SqlSession.class);
        
        // No permission getter
        DataManager<Object> dm1 = modelService.getDataManager("PermModel", sqlSession);
        assertNotNull(dm1);
        
        // With permission getter
        PermissionGetter pg = mock(PermissionGetter.class);
        modelService.setPermissionGetter(pg);
        
        DataManager<Object> dm2 = modelService.getDataManager("PermModel", sqlSession);
        assertNotNull(dm2);
        verify(pg).getPermission(any());
    }

    @Test
    void eventListeners_NotifiedOnUpdate() {
        EventListener listener = mock(EventListener.class);
        modelService.setEventListeners(Collections.singletonList(listener));

        Model model = createValidModel("EventModel");

        Table table = new Table("TEST_TABLE", null);
        when(modelToTableConverter.convertToTable(eq(model), any())).thenReturn(table);

        modelService.update(model);

        verify(listener).onEvent(any());
    }
}
