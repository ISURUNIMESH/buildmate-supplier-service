package com.buildmate.supplier.controller;

import com.buildmate.supplier.config.SecurityConfig;
import com.buildmate.supplier.dto.SupplierResponse;
import com.buildmate.supplier.dto.SupplierReviewResponse;
import com.buildmate.supplier.exception.GlobalExceptionHandler;
import com.buildmate.supplier.model.SupplierStatus;
import com.buildmate.supplier.repository.ApiKeyRepository;
import com.buildmate.supplier.service.SupplierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SupplierController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SupplierSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupplierService supplierService;

    @MockitoBean
    private ApiKeyRepository apiKeyRepository;

    @Test
    void protectedEndpointRejectsMissingApiKey() throws Exception {
        mockMvc.perform(get("/suppliers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or missing API key"));
    }

    @Test
    void protectedEndpointRejectsInvalidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("bad-key")).thenReturn(false);

        mockMvc.perform(get("/suppliers").header("X-API-KEY", "bad-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointAllowsValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        when(supplierService.getAllSuppliers()).thenReturn(List.of(
                SupplierResponse.builder().id("1").companyName("Acme").build()
        ));

        mockMvc.perform(get("/suppliers").header("X-API-KEY", "good-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyName").value("Acme"));
    }

    @Test
    void swaggerApiDocsRemainAccessibleWithoutApiKey() throws Exception {
        // Springdoc endpoints are permitted; if not present in slice this may 404 — assert not 401
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();
                    assert code != 401 : "Swagger docs must not require API key";
                });
    }

    @Test
    void searchRequiresApiKey() throws Exception {
        mockMvc.perform(get("/suppliers/search").param("query", "Acme"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchWithValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        when(supplierService.searchSuppliers("Acme")).thenReturn(List.of(
                SupplierResponse.builder().id("1").companyName("Acme").build()
        ));

        mockMvc.perform(get("/suppliers/search").param("query", "Acme").header("X-API-KEY", "good-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyName").value("Acme"));
    }

    @Test
    void verifyWithValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        when(supplierService.verifySupplier("1")).thenReturn(
                SupplierResponse.builder().id("1").status(SupplierStatus.APPROVED).build()
        );

        mockMvc.perform(patch("/suppliers/1/verify").header("X-API-KEY", "good-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void addRatingWithValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        when(supplierService.addSupplierRating(eq("1"), any())).thenReturn(
                SupplierResponse.builder().id("1").rating(4.5).build()
        );

        mockMvc.perform(post("/suppliers/1/rating")
                        .header("X-API-KEY", "good-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4.5,\"comment\":\"Good\",\"reviewerName\":\"Bob\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4.5));
    }

    @Test
    void getReviewsWithValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        when(supplierService.getSupplierReviews("1")).thenReturn(List.of(
                SupplierReviewResponse.builder().id("r1").supplierId("1").rating(5.0).build()
        ));

        mockMvc.perform(get("/suppliers/1/reviews").header("X-API-KEY", "good-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(5.0));
    }

    @Test
    void getMaterialsWithValidApiKey() throws Exception {
        when(apiKeyRepository.existsByKeyValueAndActiveTrue("good-key")).thenReturn(true);
        when(supplierService.getSupplierMaterials("1")).thenReturn(List.of(Map.of("id", "m1", "name", "Cement")));

        mockMvc.perform(get("/suppliers/1/materials").header("X-API-KEY", "good-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cement"));
    }
}
