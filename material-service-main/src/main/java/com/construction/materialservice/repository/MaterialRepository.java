package com.buildmate.material.repository;

import com.buildmate.material.model.Material;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MaterialRepository extends MongoRepository<Material, String> {

    List<Material> findByCategory(String category);

    List<Material> findByNameContainingIgnoreCase(String keyword);

    List<Material> findByStockLessThanEqual(Integer stockThreshold);

    List<Material> findByFeaturedTrue();

    List<Material> findByBrandIgnoreCase(String brand);

    List<Material> findBySupplierId(String supplierId);
}
