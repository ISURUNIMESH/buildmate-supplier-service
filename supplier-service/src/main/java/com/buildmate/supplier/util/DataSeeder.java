package com.buildmate.supplier.util;

import com.buildmate.supplier.model.ApiKey;
import com.buildmate.supplier.repository.ApiKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private final ApiKeyRepository apiKeyRepository;
    private final String defaultApiKey;

    public DataSeeder(ApiKeyRepository apiKeyRepository,
                      @Value("${supplier.api-key.default:}") String defaultApiKey) {
        this.apiKeyRepository = apiKeyRepository;
        this.defaultApiKey = defaultApiKey;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Application startup: Checking for default API key");
        if (apiKeyRepository.count() == 0) {
            if (defaultApiKey == null || defaultApiKey.isBlank()) {
                logger.warn("No default API key configured. Set SUPPLIER_API_KEY to seed an initial key.");
                return;
            }
            ApiKey defaultKey = new ApiKey();
            defaultKey.setKeyValue(defaultApiKey);
            defaultKey.setClientName("Swagger");
            defaultKey.setActive(true);
            
            apiKeyRepository.save(defaultKey);
            logger.info("Default API key seeded successfully.");
        } else {
            logger.info("API key already exists, skipping seed.");
        }
    }
}
