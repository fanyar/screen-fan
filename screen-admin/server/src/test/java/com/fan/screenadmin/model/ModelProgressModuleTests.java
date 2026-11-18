package com.fan.screenadmin.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ModelProgressModuleTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void seedsWuhanBridgeDataDrivenProgress() throws Exception {
        String modelId = wuhanModelId();
        MvcResult result = mockMvc.perform(get("/api/models/{id}/progress-tree", modelId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode tree = objectMapper.readTree(content(result));

        assertEquals(1, tree.get("nodes").size());
        assertEquals("第一联", tree.get("nodes").get(0).get("name").asText());
        assertEquals(9, tree.get("nodes").get(0).get("children").size());
        assertTrue(tree.get("summary").get("total").asInt() >= 27);
        assertTrue(tree.get("summary").get("delayed").asInt() >= 1);
        assertTrue(tree.get("summary").get("progressPercent").asInt() > 0);
        assertTrue(tree.get("unboundNodeNames").isEmpty());

        MvcResult summaryResult = mockMvc.perform(get("/api/models/{id}/progress-summary", modelId))
                .andExpect(status().isOk())
                .andReturn();
        assertFalse(objectMapper.readTree(content(summaryResult)).get("currentStage").asText().isBlank());

        MvcResult snapshots = mockMvc.perform(get("/api/models/{id}/progress/snapshots", modelId))
                .andExpect(status().isOk())
                .andReturn();
        assertFalse(objectMapper.readTree(content(snapshots)).isEmpty());
    }

    @Test
    void protectsAndValidatesProgressWrites() throws Exception {
        String modelId = createModel("进度权限验证");
        mockMvc.perform(put("/api/admin/models/{id}/progress", modelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nodes\":[]}"))
                .andExpect(status().isUnauthorized());

        String token = loginToken();
        mockMvc.perform(put("/api/admin/models/{id}/progress", modelId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nodes":[{"name":"错误状态","nodeType":"member","meshKey":"bad","status":"unknown"}]}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/admin/models/{id}", modelId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void savesCalculatesSnapshotsAndImportsCsv() throws Exception {
        String modelId = createModel("进度计算验证");
        String token = loginToken();
        String request = """
                {
                  "nodes": [
                    {"id":"unit","name":"第一联","nodeType":"unit","status":"in_progress"},
                    {"id":"span","parentId":"unit","name":"第1跨","nodeType":"span","status":"in_progress","weight":2},
                    {"id":"done","parentId":"span","name":"钢桁梁","nodeType":"member","meshKey":"Unit-01.Span-01.girder","status":"completed","weight":2},
                    {"id":"doing","parentId":"span","name":"桥面板","nodeType":"member","meshKey":"Unit-01.Span-01.deck","status":"in_progress","progressPercent":50,"weight":1,"plannedEndDate":"2099-12-31"},
                    {"id":"late","parentId":"span","name":"下层梁","nodeType":"member","meshKey":"Unit-01.Span-01.lower-beam","status":"in_progress","progressPercent":20,"weight":1,"plannedEndDate":"2020-01-01","delayReason":"供货延迟"}
                  ]
                }
                """;
        MvcResult saved = mockMvc.perform(put("/api/admin/models/{id}/progress", modelId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode summary = objectMapper.readTree(content(saved)).get("summary");
        assertEquals(3, summary.get("total").asInt());
        assertEquals(1, summary.get("completed").asInt());
        assertEquals(1, summary.get("inProgress").asInt());
        assertEquals(1, summary.get("delayed").asInt());
        assertEquals(0, summary.get("notStarted").asInt());
        assertEquals(68, summary.get("progressPercent").asInt());

        mockMvc.perform(get("/api/admin/models/{id}/progress/audits", modelId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String csv = """
                nodeType,name,meshKey,status,progressPercent,weight,plannedEndDate
                member,导入梁,imported-beam,completed,100,2,2099-12-31
                member,导入板,imported-deck,in_progress,50,1,2099-12-31
                """;
        MvcResult imported = mockMvc.perform(post("/api/admin/models/{id}/progress/import", modelId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                        .content(csv))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(2, objectMapper.readTree(content(imported)).get("summary").get("total").asInt());

        String publicTree = content(mockMvc.perform(get("/api/models/{id}/progress-tree", modelId))
                .andExpect(status().isOk())
                .andReturn());
        assertEquals("导入梁", objectMapper.readTree(publicTree).get("nodes").get(0).get("name").asText());

        mockMvc.perform(delete("/api/admin/models/{id}", modelId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    private String wuhanModelId() throws Exception {
        String list = content(mockMvc.perform(get("/api/models?pageSize=48")).andReturn());
        for (JsonNode item : objectMapper.readTree(list).get("items")) {
            if ("武汉长江大桥".equals(item.get("title").asText())) {
                return item.get("id").asText();
            }
        }
        throw new IllegalStateException("武汉长江大桥模型未初始化");
    }

    private String createModel(String title) throws Exception {
        String token = loginToken();
        MvcResult result = mockMvc.perform(post("/api/admin/models")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "category": "桥梁",
                                  "fileUrl": "/platform/models/bridge-sample.glb",
                                  "status": "published"
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(content(result)).get("id").asText();
    }

    private String loginToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(content(result)).get("token").asText();
    }

    private String content(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
