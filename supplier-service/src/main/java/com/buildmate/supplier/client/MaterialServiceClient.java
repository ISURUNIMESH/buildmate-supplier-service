package com.buildmate.supplier.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Reads materials for a supplier from Material service (does not duplicate catalog data).
 */
@Component
public class MaterialServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(MaterialServiceClient.class);

    private final RestClient restClient;

    public MaterialServiceClient(
            @Value("${buildmate.material-service.url:http://localhost:8085}") String baseUrl,
            @Value("${MATERIAL_API_KEY:}") String apiKey) {
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        if (apiKey != null && !apiKey.isBlank()) {
            builder = builder.defaultHeader("X-API-KEY", apiKey);
        }
        this.restClient = builder.build();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getMaterialsBySupplierId(String supplierId) {
        try {
            Map<String, Object>[] body = restClient.get()
                    .uri("/materials/supplier/{supplierId}", supplierId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map[].class);
            if (body == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(body);
        } catch (RestClientException ex) {
            logger.error("Unable to load materials from Material service: {}", ex.getMessage());
            throw new IllegalStateException(
                    "Unable to load materials for supplier from Material service: " + ex.getMessage(),
                    ex);
        }
    }
}
