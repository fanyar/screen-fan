package com.fan.screenadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScreenAdminApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void seedsPublicLists() throws Exception {
        assertEquals(6, listSize("/api/works"));
        assertEquals(3, listSize("/api/comics"));
        assertEquals(10, listSize("/api/movies"));
        assertEquals(10, listSize("/api/anime"));
        assertEquals(24, listTotal("/api/articles"));
        String movieList = getContent("/api/movies");
        assertEquals(true, objectMapper.readTree(movieList).get("items").get(0).get("playUrl").asText().startsWith("http://vedio.yxdsrq.com/"));
        String animeList = getContent("/api/anime");
        assertEquals(true, objectMapper.readTree(animeList).get("items").get(0).get("playUrl").asText().startsWith("http://vedio.yxdsrq.com/"));
        String homeConfig = getContent("/api/home-config");
        assertEquals("FAN · 凡集", objectMapper.readTree(homeConfig).get("platformName").asText());
        assertEquals(3, objectMapper.readTree(homeConfig).get("features").size());
        String articles = getContent("/api/articles");
        String articleId = objectMapper.readTree(articles).get("items").get(0).get("id").asText();
        String articleDetail = getContent("/api/articles/" + articleId);
        assertEquals("published", objectMapper.readTree(articleDetail).get("status").asText());
        assertEquals(false, objectMapper.readTree(articleDetail).get("content").isNull());
    }

    @Test
    @Order(2)
    void protectsAdminWrites() throws Exception {
        mockMvc.perform(post("/api/admin/works")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/admin/home-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    void createsNewItemFirstAndUploadsCover() throws Exception {
        String token = loginToken();
        String createResponse = mockMvc.perform(post("/api/admin/works")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "新增作品结构化验证",
                                  "cover": "",
                                  "desc": "新增后应出现在第一位，并保留结构化详情",
                                  "tags": ["排序"],
                                  "year": 2026,
                                  "role": "前端负责人",
                                  "period": "2026.10",
                                  "projectUrl": "https://example.com/work",
                                  "techStack": ["Vue3", "Vite"],
                                  "content": "## 项目过程\\n\\n- 梳理信息层级\\n- 处理图表状态",
                                  "result": "## 成果\\n\\n- 首屏决策效率提升",
                                  "images": ["/uploads/work-01.png", "/uploads/work-02.png"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String id = objectMapper.readTree(createResponse).get("id").asText();
        var workDetail = objectMapper.readTree(getContent("/api/works/" + id));
        assertEquals("前端负责人", workDetail.get("role").asText());
        assertEquals("2026.10", workDetail.get("period").asText());
        assertEquals(2, workDetail.get("techStack").size());
        assertEquals(2, workDetail.get("images").size());
        assertEquals(true, workDetail.get("content").asText().contains("项目过程"));

        String list = mockMvc.perform(get("/api/works"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertEquals("新增作品结构化验证", objectMapper.readTree(list).get("items").get(0).get("title").asText());

        mockMvc.perform(put("/api/admin/works/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "新增作品结构化验证",
                                  "cover": "",
                                  "desc": "非法链接应被拒绝",
                                  "tags": ["排序"],
                                  "year": 2026,
                                  "role": "前端负责人",
                                  "period": "2026.10",
                                  "projectUrl": "javascript:alert(1)",
                                  "techStack": ["Vue3"],
                                  "content": "",
                                  "result": "",
                                  "images": []
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/admin/works/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-cover.svg",
                "image/svg+xml",
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1\" height=\"1\"></svg>".getBytes()
        );
        String uploadResponse = mockMvc.perform(multipart("/api/admin/uploads")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String uploadUrl = objectMapper.readTree(uploadResponse).get("url").asText();
        mockMvc.perform(get(uploadUrl)).andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void updatesHomeConfig() throws Exception {
        String token = loginToken();
        mockMvc.perform(put("/api/admin/home-config")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platformName": "配置后的平台",
                                  "heroBadge": "配置后的标签",
                                  "heroTitle": "配置后的主标题",
                                  "heroDescription": "配置后的主视觉描述",
                                  "heroVideoUrl": null,
                                  "heroPrimaryButtonText": "进入平台",
                                  "heroSecondaryButtonText": "了解产品",
                                  "introTitle": "配置后的介绍标题",
                                  "introDescription": "配置后的介绍内容",
                                  "introBackgroundUrl": null,
                                  "featureTitle": "产品特性",
                                  "features": [
                                    {"title": "配置后的特性", "desc": "配置后的特性描述", "image": null}
                                  ],
                                  "contactTitle": "联系我们",
                                  "contactDescription": "配置后的联系描述",
                                  "contactPhone": "13800000000",
                                  "footerText": "配置后的页脚"
                                }
                                """))
                .andExpect(status().isOk());

        String config = getContent("/api/home-config");
        assertEquals("配置后的平台", objectMapper.readTree(config).get("platformName").asText());
        assertEquals("配置后的特性", objectMapper.readTree(config).get("features").get(0).get("title").asText());
    }

    @Test
    @Order(5)
    void managesArticleDraftAndPublication() throws Exception {
        String token = loginToken();
        String createResponse = mockMvc.perform(post("/api/admin/articles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "草稿文章验证",
                                  "desc": "草稿不应出现在公开列表",
                                  "tags": ["测试"],
                                  "date": "2026-10-27",
                                  "content": "## 草稿正文\\n\\n等待发布。",
                                  "status": "draft"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String id = objectMapper.readTree(createResponse).get("id").asText();

        assertEquals(24, listTotal("/api/articles"));
        String adminArticles = mockMvc.perform(get("/api/admin/articles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertEquals(25, objectMapper.readTree(adminArticles).size());
        mockMvc.perform(get("/api/articles/{id}", id))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/admin/articles/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "草稿文章验证",
                                  "desc": "发布后的文章会出现在公开列表",
                                  "tags": ["测试"],
                                  "date": "2026-10-27",
                                  "content": "## 已发布正文\\n\\n- 支持列表\\n- 支持 Markdown",
                                  "status": "published"
                                }
                                """))
                .andExpect(status().isOk());

        String detail = getContent("/api/articles/" + id);
        assertEquals("published", objectMapper.readTree(detail).get("status").asText());
        assertEquals("## 已发布正文\n\n- 支持列表\n- 支持 Markdown", objectMapper.readTree(detail).get("content").asText());

        mockMvc.perform(delete("/api/admin/articles/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(6)
    void filtersPaginatesAndShowsContentDetails() throws Exception {
        String works = getContent("/api/works?keyword=ECharts&page=1&pageSize=3");
        assertEquals(4, objectMapper.readTree(works).get("total").asLong());
        assertEquals(3, objectMapper.readTree(works).get("items").size());
        String workId = objectMapper.readTree(works).get("items").get(0).get("id").asText();
        mockMvc.perform(get("/api/works/{id}", workId))
                .andExpect(status().isOk());

        String comics = getContent("/api/comics?page=1&pageSize=2");
        assertEquals(3, objectMapper.readTree(comics).get("total").asLong());
        assertEquals(2, objectMapper.readTree(comics).get("items").size());
        String comicId = objectMapper.readTree(comics).get("items").get(0).get("id").asText();
        mockMvc.perform(get("/api/comics/{id}", comicId))
                .andExpect(status().isOk());

        String movies = getContent("/api/movies?keyword=现实实验");
        assertEquals(1, objectMapper.readTree(movies).get("total").asLong());
        String movieId = objectMapper.readTree(movies).get("items").get(0).get("id").asText();
        mockMvc.perform(get("/api/movies/{id}", movieId))
                .andExpect(status().isOk());

        String facets = getContent("/api/content-facets");
        assertEquals(false, objectMapper.readTree(facets).get("workTags").isEmpty());
        assertEquals(false, objectMapper.readTree(facets).get("comicStatuses").isEmpty());
        assertEquals(false, objectMapper.readTree(facets).get("movieGenres").isEmpty());
        assertEquals(false, objectMapper.readTree(facets).get("animeGenres").isEmpty());
    }

    @Test
    @Order(10)
    void recordsUniqueVisitorStats() throws Exception {
        String visitorA = "visitor-" + UUID.randomUUID();
        String visitorB = "visitor-" + UUID.randomUUID();
        String works = getContent("/api/works");
        String workId = objectMapper.readTree(works).get("items").get(0).get("id").asText();

        String first = mockMvc.perform(get("/api/works/{id}", workId)
                        .header("X-Visitor-Key", visitorA))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        long firstVisitorViews = objectMapper.readTree(first).get("viewCount").asLong();
        long firstVisitorCount = objectMapper.readTree(first).get("visitorCount").asLong();
        String repeated = mockMvc.perform(get("/api/works/{id}", workId)
                        .header("X-Visitor-Key", visitorA))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertEquals(firstVisitorViews + 1, objectMapper.readTree(repeated).get("viewCount").asLong());
        assertEquals(firstVisitorCount, objectMapper.readTree(repeated).get("visitorCount").asLong());

        String secondVisitor = mockMvc.perform(get("/api/works/{id}", workId)
                        .header("X-Visitor-Key", visitorB))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertEquals(firstVisitorViews + 2, objectMapper.readTree(secondVisitor).get("viewCount").asLong());
        assertEquals(firstVisitorCount + 1, objectMapper.readTree(secondVisitor).get("visitorCount").asLong());
    }

    @Test
    @Order(7)
    void tracksArticleViewsAndManagesContactMessages() throws Exception {
        String articles = getContent("/api/articles");
        String articleId = objectMapper.readTree(articles).get("items").get(0).get("id").asText();
        long firstViewCount = objectMapper.readTree(getContent("/api/articles/" + articleId)).get("viewCount").asLong();
        long secondViewCount = objectMapper.readTree(getContent("/api/articles/" + articleId)).get("viewCount").asLong();
        assertEquals(firstViewCount + 1, secondViewCount);

        String messageResponse = mockMvc.perform(post("/api/contact-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "测试访客",
                                  "contact": "test@example.com",
                                  "content": "希望增加更多作品详情。"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String messageId = objectMapper.readTree(messageResponse).get("id").asText();

        mockMvc.perform(get("/api/admin/contact-messages"))
                .andExpect(status().isUnauthorized());

        String token = loginToken();
        String dashboard = mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        var dashboardNode = objectMapper.readTree(dashboard);
        assertEquals(6, dashboardNode.get("workCount").asLong());
        assertEquals(3, dashboardNode.get("comicCount").asLong());
        assertEquals(10, dashboardNode.get("movieCount").asLong());
        assertEquals(10, dashboardNode.get("animeCount").asLong());
        assertEquals(false, dashboardNode.get("popularArticles").isEmpty());

        mockMvc.perform(put("/api/admin/contact-messages/{id}/handled", messageId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String messages = mockMvc.perform(get("/api/admin/contact-messages")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertEquals(true, objectMapper.readTree(messages).get(0).get("handled").asBoolean());

        mockMvc.perform(delete("/api/admin/contact-messages/{id}", messageId)
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
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private int listSize(String path) throws Exception {
        String content = getContent(path);
        return objectMapper.readTree(content).get("items").size();
    }

    private int listTotal(String path) throws Exception {
        String content = getContent(path);
        return objectMapper.readTree(content).get("total").asInt();
    }

    private String getContent(String path) throws Exception {
        return mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @Order(6)
    void rejectsInvalidLogin() throws Exception {
        String wrongPassword = mockMvc.perform(post("/api/admin/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\"admin\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertEquals(400, objectMapper.readTree(wrongPassword).get("code").asInt());
        assertEquals("账号或密码不正确", objectMapper.readTree(wrongPassword).get("message").asText());

        String blankAccount = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertEquals("账号和密码不能为空", objectMapper.readTree(blankAccount).get("message").asText());
    }

    @Test
    @Order(7)
    void logoutInvalidatesToken() throws Exception {
        String token = loginToken();
        mockMvc.perform(get("/api/admin/articles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/articles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    void healthEndpointReportsUp() throws Exception {
        String health = mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertEquals("UP", objectMapper.readTree(health).get("status").asText());
    }

    @Test
    @Order(9)
    void rejectsUnsupportedUploadType() throws Exception {
        String token = loginToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "not an image".getBytes(StandardCharsets.UTF_8)
        );
        String uploadResponse = mockMvc.perform(multipart("/api/admin/uploads")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        assertEquals("文件格式不支持",
                objectMapper.readTree(uploadResponse).get("message").asText());
    }
}
