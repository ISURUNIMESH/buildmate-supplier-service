package com.buildmate.supplier.repository;

import com.buildmate.supplier.model.ApiKey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {
    Optional<ApiKey> findByKeyValue(String keyValue);
    Optional<ApiKey> findByClientName(String clientName);
    List<ApiKey> findByActive(Boolean active);
}
