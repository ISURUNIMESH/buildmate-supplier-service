package com.buildmate.orderinventory.repository;

import com.buildmate.orderinventory.model.ApiKey;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {
    boolean existsByKeyValueAndActiveTrue(String keyValue);
}
