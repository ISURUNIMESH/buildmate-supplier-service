package com.buildmate.payment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.buildmate.payment.model.ApiKey;

public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {

    boolean existsByKeyValueAndActiveTrue(String keyValue);
}
