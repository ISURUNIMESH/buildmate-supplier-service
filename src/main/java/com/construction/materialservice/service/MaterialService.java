package com.construction.materialservice.service;

import com.construction.materialservice.dto.BulkStockUpdateRequest;
import com.construction.materialservice.model.Material;
import com.construction.materialservice.repository.MaterialRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    // Get All Materials
    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    // Get Material By ID
    public Material getMaterialById(String id) {

    return materialRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Material not found"));
   }
    // Add Material
    public Material addMaterial(Material material) {
        material.setCreatedAt(LocalDateTime.now());
        material.setUpdatedAt(LocalDateTime.now());
        return materialRepository.save(material);
    }

    // Update Material
    public Material updateMaterial(String id, Material material) {

        Material existing = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        existing.setName(material.getName());
        existing.setDescription(material.getDescription());
        existing.setCategory(material.getCategory());
        existing.setBrand(material.getBrand());
        existing.setPrice(material.getPrice());
        existing.setStock(material.getStock());
        existing.setUnit(material.getUnit());
        existing.setSupplierId(material.getSupplierId());
        existing.setImageUrl(material.getImageUrl());
        existing.setFeatured(material.isFeatured());
        existing.setUpdatedAt(LocalDateTime.now());

        return materialRepository.save(existing);
    }

    // Delete Material
    public void deleteMaterial(String id) {
        materialRepository.deleteById(id);
    }

    // Search By Category
    public List<Material> getByCategory(String category) {
        return materialRepository.findByCategory(category);
    }

    // Search By Brand
    public List<Material> getByBrand(String brand) {
        return materialRepository.findByBrand(brand);
    }

    // Search By Name
    public List<Material> searchMaterials(String keyword) {
        return materialRepository.findByNameContainingIgnoreCase(keyword);
    }

    // Featured Materials
    public List<Material> getFeaturedMaterials() {
        return materialRepository.findByFeaturedTrue();
    }

    // Low Stock
    public List<Material> getLowStock() {
        return materialRepository.findByStockLessThan(150);
    }

    // Update Stock
    public Material updateStock(String id, Integer stock) {

        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        material.setStock(stock);
        material.setUpdatedAt(LocalDateTime.now());

        return materialRepository.save(material);
    }

    // Bulk Stock Update
    public List<Material> bulkUpdateStock(List<BulkStockUpdateRequest> requests) {

        for (BulkStockUpdateRequest request : requests) {

            Material material = materialRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Material not found"));

            material.setStock(request.getStock());
            material.setUpdatedAt(LocalDateTime.now());

            materialRepository.save(material);
        }

        return materialRepository.findAll();
    }

    // Update Price
    public Material updatePrice(String id, Double price) {

        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        material.setPrice(price);
        material.setUpdatedAt(LocalDateTime.now());

        return materialRepository.save(material);
    }
}