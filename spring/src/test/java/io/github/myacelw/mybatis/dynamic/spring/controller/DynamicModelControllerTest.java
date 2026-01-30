package io.github.myacelw.mybatis.dynamic.spring.controller;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.PageResult;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.ModelService;
import io.github.myacelw.mybatis.dynamic.core.service.chain.PageChain;
import io.github.myacelw.mybatis.dynamic.core.metadata.Permission;
import io.github.myacelw.mybatis.dynamic.spring.hook.CurrentUserHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DynamicModelControllerTest {

    private MockMvc mockMvc;

    private ModelService modelService;

    private CurrentUserHolder currentUserHolder;

    private DataManager<Object> dataManager;

    @BeforeEach
    public void setup() {
        modelService = Mockito.mock(ModelService.class);
        currentUserHolder = Mockito.mock(CurrentUserHolder.class);
        
        // Manual instantiation of the controller with mocks
        DynamicModelController controller = new DynamicModelController(modelService, currentUserHolder);
        
        // Standalone setup - no need for @WebMvcTest or full Spring context
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        dataManager = Mockito.mock(DataManager.class);
        // Note: The controller logic calls modelService.getModel first, then createDataManager.
        // We don't need to mock getDataManager if we mock the underlying service calls.
        // But wait, the original test mocked modelService.getDataManager which is NOT called in the new controller logic shown?
        // Let's verify the controller logic. 
        // Controller calls: modelService.getModel(name) -> modelService.createDataManager(model, perm, null)
        
        io.github.myacelw.mybatis.dynamic.core.metadata.Model model = new io.github.myacelw.mybatis.dynamic.core.metadata.Model();
        model.setName("User");
        
        // Stub getModel
        when(modelService.getModel("User")).thenReturn(model);

        // Stub createDataManager
        when(modelService.createDataManager(eq(model), any(), any())).thenReturn(dataManager);
        
        // For default dataManager.getModel() calls if any
        when(dataManager.getModel()).thenReturn(model);
    }

    @Test
    public void testGetById() throws Exception {
        Map<String, Object> user = new HashMap<>();
        user.put("id", "1");
        user.put("name", "zhang");
        when(dataManager.getById(any())).thenReturn(user);

        mockMvc.perform(get("/api/dynamic/User/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("zhang"));
    }

    @Test
    public void testCreate() throws Exception {
        when(dataManager.insert(any())).thenReturn("100");

        mockMvc.perform(post("/api/dynamic/User")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"test\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    public void testDelete() throws Exception {
        when(dataManager.delete(any(Object.class))).thenReturn(true);

        mockMvc.perform(delete("/api/dynamic/User/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testList() throws Exception {
        // Use real PageChain to avoid final method mocking issues
        PageChain pageChain = new PageChain(dataManager);
        when(dataManager.pageChain()).thenReturn(pageChain);

        PageResult<Map<String, Object>> result = new PageResult<>(Collections.emptyList(), 0);
        // Mock the execution of the command
        when(dataManager.execCommand(any())).thenReturn(result);

        mockMvc.perform(get("/api/dynamic/User?name=zhang&page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    public void testPermissionIntegration() throws Exception {
        io.github.myacelw.mybatis.dynamic.core.metadata.Model model = new io.github.myacelw.mybatis.dynamic.core.metadata.Model();
        model.setName("User");
        when(modelService.getModel("User")).thenReturn(model);

        io.github.myacelw.mybatis.dynamic.core.metadata.Permission permission = new io.github.myacelw.mybatis.dynamic.core.metadata.Permission();
        when(currentUserHolder.getCurrentUserPermission(model)).thenReturn(permission);

        DataManager<Object> dm = Mockito.mock(DataManager.class);
        // Expect createDataManager to be called with the permission
        when(modelService.createDataManager(eq(model), eq(permission), any())).thenReturn(dm);

        // Mock getById on the new DM
        when(dm.getById(any())).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/api/dynamic/User/1"))
                .andExpect(status().isOk());

        // Verify createDataManager was called with permission
        Mockito.verify(modelService).createDataManager(eq(model), eq(permission), any());
    }
}
