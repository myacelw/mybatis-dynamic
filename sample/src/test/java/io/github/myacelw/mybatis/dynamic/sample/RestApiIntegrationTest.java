package io.github.myacelw.mybatis.dynamic.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testDicCrud() throws Exception {
        String uniqueName = "D" + System.currentTimeMillis() / 1000;
        // Create
        Map<String, Object> dic = new HashMap<>();
        dic.put("name", uniqueName);
        dic.put("dicDirectoryId", 1);
        dic.put("status", "Valid");
        
        MvcResult createResult = mockMvc.perform(post("/api/dynamic/Dic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dic)))
                .andExpect(status().isOk())
                .andReturn();
        
        String idStr = createResult.getResponse().getContentAsString();
        // ID might be quoted or not depending on type. Assume generic object.
        // If it's a string, it might be "123". If integer, 123.
        Object id = idStr;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            id = idStr.replace("\"", "");
        }
        
        // List
        mockMvc.perform(get("/api/dynamic/Dic")
                .param("name", uniqueName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value(uniqueName));
                
        // Update
        dic.put("id", id); 
        dic.put("name", uniqueName + " Updated");
        
        mockMvc.perform(put("/api/dynamic/Dic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dic)))
                .andExpect(status().isOk());
                
        // Get By ID
        mockMvc.perform(get("/api/dynamic/Dic/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(uniqueName + " Updated"));
                
        // Delete
        mockMvc.perform(delete("/api/dynamic/Dic/" + id))
                .andExpect(status().isOk());
    }
}
