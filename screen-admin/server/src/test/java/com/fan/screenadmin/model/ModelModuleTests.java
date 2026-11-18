package com.fan.screenadmin.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ModelModuleTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicListOnlyContainsPublishedModels() throws Exception {
        String content = mockMvc.perform(get("/api/models"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertTrue(objectMapper.readTree(content).get("total").asLong() >= 1);
        assertEquals("桥梁", objectMapper.readTree(content).get("items").get(0).get("category").asText());
    }

    @Test
    void managesModelLifecycleAndUploadsGlb() throws Exception {
        mockMvc.perform(post("/api/admin/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        String token = loginToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bridge.glb",
                "model/gltf-binary",
                new byte[]{0x47, 0x4c, 0x54, 0x46}
        );
        String uploadResponse = mockMvc.perform(multipart("/api/admin/uploads")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String fileUrl = objectMapper.readTree(uploadResponse).get("url").asText();
        assertTrue(fileUrl.startsWith("/uploads/"));

        String request = """
                {
                  "title": "测试桥梁模型",
                  "category": "桥梁",
                  "cover": "",
                  "fileUrl": "%s",
                  "desc": "后台模型管理验证",
                  "tags": ["测试"],
                  "status": "draft"
                }
                """.formatted(fileUrl);
        String createResponse = mockMvc.perform(post("/api/admin/models")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String id = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/models/{id}", id)).andExpect(status().isBadRequest());

        String publishRequest = request.replace("\"draft\"", "\"published\"");
        mockMvc.perform(put("/api/admin/models/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publishRequest))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/models/{id}", id)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/models/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    private String loginToken() throws Exception {
        String response = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("token").asText();
    }
}
