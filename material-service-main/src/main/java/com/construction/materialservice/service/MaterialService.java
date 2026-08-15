package com.buildmate.material.service;

import com.buildmate.material.dto.BulkStockUpdateRequest;
import com.buildmate.material.dto.MaterialImageRequest;
import com.buildmate.material.exception.ResourceNotFoundException;
import com.buildmate.material.model.Material;
import com.buildmate.material.model.MaterialImage;
import com.buildmate.material.producer.MaterialEventPublisher;
import com.buildmate.material.repository.MaterialImageRepository;
import com.buildmate.material.repository.MaterialRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialImageRepository materialImageRepository;
    private final MaterialEventPublisher materialEventPublisher;

    public MaterialService(
            MaterialRepository materialRepository,
            MaterialImageRepository materialImageRepository,
            MaterialEventPublisher materialEventPublisher) {
        this.materialRepository = materialRepository;
        this.materialImageRepository = materialImageRepository;
        this.materialEventPublisher = materialEventPublisher;
    }

    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    public Material getMaterialById(String id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
    }

    public Material addMaterial(Material material) {
        if (material.getFeatured() == null) {
            material.setFeatured(Boolean.FALSE);
        }
        material.setCreatedAt(LocalDateTime.now());
        material.setUpdatedAt(LocalDateTime.now());
        Material saved = materialRepository.save(material);
        materialEventPublisher.publishCreated(saved);
        return saved;
    }

    public Material updateMaterial(String id, Material material) {
        Material existing = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));

        existing.setName(material.getName());
        existing.setDescription(material.getDescription());
        existing.setCategory(material.getCategory());
        existing.setPrice(material.getPrice());
        existing.setStock(material.getStock());
        existing.setUnit(material.getUnit());
        existing.setSupplierId(material.getSupplierId());
        existing.setBrand(material.getBrand());
        existing.setFeatured(material.getFeatured() != null ? material.getFeatured() : Boolean.FALSE);
        existing.setUpdatedAt(LocalDateTime.now());

        Material saved = materialRepository.save(existing);
        materialEventPublisher.publishUpdated(saved);
        return saved;
    }

    public void deleteMaterial(String id) {
        Material existing = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
        materialRepository.deleteById(id);
        materialEventPublisher.publishDeleted(existing);
    }

    public List<Material> getByCategory(String category) {
        return materialRepository.findByCategory(category);
    }

    public List<Material> getLowStock() {
        return materialRepository.findByStockLessThanEqual(150);
    }

    public List<Material> searchMaterials(String keyword) {
        return materialRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Material> getFeaturedMaterials() {
        return materialRepository.findByFeaturedTrue();
    }

    public List<Material> getByBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Brand is required");
        }
        return materialRepository.findByBrandIgnoreCase(brand.trim());
    }

    public Material updateStock(String id, Integer stock) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));

        material.setStock(stock);
        material.setUpdatedAt(LocalDateTime.now());

        Material saved = materialRepository.save(material);
        materialEventPublisher.publishStockUpdated(saved);
        return saved;
    }

    public Material updatePrice(String id, Double price) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));

        material.setPrice(price);
        material.setUpdatedAt(LocalDateTime.now());

        Material saved = materialRepository.save(material);
        materialEventPublisher.publishUpdated(saved);
        return saved;
    }

    public List<Material> bulkUpdateStock(List<BulkStockUpdateRequest> updates) {
        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException("At least one stock update is required");
        }
        List<Material> updated = new ArrayList<>();
        for (BulkStockUpdateRequest item : updates) {
            updated.add(updateStock(item.getId(), item.getStock()));
        }
        return updated;
    }

    public MaterialImage addMaterialImage(String materialId, MaterialImageRequest request) {
        getMaterialById(materialId);

        String url = request.getImageUrl().trim();
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new IllegalArgumentException("imageUrl must be an http(s) URL");
        }

        MaterialImage image = new MaterialImage();
        image.setMaterialId(materialId);
        image.setFileName(request.getFileName().trim());
        image.setImageUrl(url);
        image.setUploadedAt(LocalDateTime.now());
        return materialImageRepository.save(image);
    }

    public List<Material> getBySupplierId(String supplierId) {
        return materialRepository.findBySupplierId(supplierId);
    }
}
