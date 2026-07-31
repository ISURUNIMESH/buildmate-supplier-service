package com.construction.materialservice.repository;

import com.construction.materialservice.model.Brand;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BrandRepository extends MongoRepository<Brand, String> {

    Optional<Brand> findByName(String name);

    boolean existsByName(String name);
}