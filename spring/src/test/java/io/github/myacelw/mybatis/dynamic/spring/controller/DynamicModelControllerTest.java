package io.github.myacelw.mybatis.dynamic.spring.controller;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.query.PageResult;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.ModelService;
import io.github.myacelw.mybatis.dynamic.core.service.chain.PageChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DynamicModelController.class)
public class DynamicModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModelService modelService;

    private DataManager<Object> dataManager;

    @BeforeEach
    public void setup() {
        dataManager = Mockito.mock(DataManager.class);
        when(modelService.getDataManager(anyString(), any())).thenReturn(dataManager);
        Model model = new Model();
        model.setName("User");
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
}
