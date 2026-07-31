package com.construction.materialservice.repository;

import com.construction.materialservice.model.Material;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MaterialRepository extends MongoRepository<Material, String> {

    // Search by Category
    List<Material> findByCategory(String category);

    // Search by Name
    List<Material> findByNameContainingIgnoreCase(String keyword);

    // Low Stock
    List<Material> findByStockLessThan(Integer stock);

    // Search by Brand
    List<Material> findByBrand(String brand);

    // Featured Materials
    List<Material> findByFeaturedTrue();
}