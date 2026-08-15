package com.buildmate.material.controller;

import com.buildmate.material.config.SecurityConfig;
import com.buildmate.material.exception.GlobalExceptionHandler;
import com.buildmate.material.model.Material;
import com.buildmate.material.model.MaterialImage;
import com.buildmate.material.repository.ApiKeyRepository;
import com.buildmate.material.service.MaterialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MaterialController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MaterialSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MaterialService materialService;

    @MockitoBean
    private ApiKeyRepository apiKeyRepository;

    @Test
    void protectedEndpointRejectsMissingApiKey() throws Exception {
        mockMvc.perform(get("/materials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or missing API key"));
    }

    @Test
    void protectedEndpointRejectsInvalidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("bad-key")).thenReturn(false);

        mockMvc.perform(get("/materials").header("X-API-KEY", "bad-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointAllowsValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        Material material = new Material();
        material.setId("1");
        material.setName("Cement");
        when(materialService.getAllMaterials()).thenReturn(List.of(material));

        mockMvc.perform(get("/materials").header("X-API-KEY", "good-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cement"));
    }

    @Test
    void swaggerApiDocsRemainAccessibleWithoutApiKey() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();
                    assert code != 401 : "Swagger docs must not require API key";
                });
    }

    @Test
    void actuatorHealthRemainsAccessibleWithoutApiKey() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();
                    assert code != 401 : "Actuator health must not require API key";
                });
    }

    @Test
    void featuredRequiresApiKey() throws Exception {
        mockMvc.perform(get("/materials/featured"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void featuredWithValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        Material material = new Material();
        material.setId("1");
        material.setName("Cement");
        material.setFeatured(true);
        when(materialService.getFeaturedMaterials()).thenReturn(List.of(material));

        mockMvc.perform(get("/materials/featured").header("X-API-KEY", "good-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cement"));
    }

    @Test
    void brandWithValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        when(materialService.getByBrand("Holcim")).thenReturn(List.of());

        mockMvc.perform(get("/materials/brand/Holcim").header("X-API-KEY", "good-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void categoryWithValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        when(materialService.getByCategory("Cement")).thenReturn(List.of());

        mockMvc.perform(get("/materials/category/Cement").header("X-API-KEY", "good-key"))
                .andExpect(status().isOk());
    }

    @Test
    void bulkStockRequiresApiKey() throws Exception {
        mockMvc.perform(patch("/materials/bulk-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"id\":\"1\",\"stock\":10}]"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bulkStockWithValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        Material material = new Material();
        material.setId("1");
        material.setStock(10);
        when(materialService.bulkUpdateStock(any())).thenReturn(List.of(material));

        mockMvc.perform(patch("/materials/bulk-stock")
                        .header("X-API-KEY", "good-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"id\":\"1\",\"stock\":10}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value(10));
    }

    @Test
    void addImageRequiresApiKey() throws Exception {
        mockMvc.perform(post("/materials/1/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"a.png\",\"imageUrl\":\"https://cdn.example.com/a.png\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addImageWithValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        MaterialImage image = new MaterialImage();
        image.setMaterialId("1");
        image.setFileName("a.png");
        image.setImageUrl("https://cdn.example.com/a.png");
        when(materialService.addMaterialImage(eq("1"), any())).thenReturn(image);

        mockMvc.perform(post("/materials/1/image")
                        .header("X-API-KEY", "good-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"a.png\",\"imageUrl\":\"https://cdn.example.com/a.png\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.example.com/a.png"));
    }
}
