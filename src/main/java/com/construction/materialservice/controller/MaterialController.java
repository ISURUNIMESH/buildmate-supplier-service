package com.construction.materialservice.controller;

import com.construction.materialservice.dto.BulkStockUpdateRequest;
import com.construction.materialservice.dto.MaterialImageRequest;
import com.construction.materialservice.dto.PriceUpdateRequest;
import com.construction.materialservice.dto.StockUpdateRequest;
import com.construction.materialservice.model.Material;
import com.construction.materialservice.model.MaterialImage;
import com.construction.materialservice.service.MaterialImageService;
import com.construction.materialservice.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/materials")
@CrossOrigin(origins = "*")
public class MaterialController {

    private final MaterialService materialService;
    private final MaterialImageService materialImageService;

    public MaterialController(MaterialService materialService,
                              MaterialImageService materialImageService) {
        this.materialService = materialService;
        this.materialImageService = materialImageService;
    }

    // Get All Materials
    @GetMapping
    public List<Material> getAllMaterials() {
        return materialService.getAllMaterials();
    }

    // Get Material By ID
    @GetMapping("/{id}")
    public ResponseEntity<Material> getMaterialById(@PathVariable String id) {

        return ResponseEntity.ok(
                materialService.getMaterialById(id)
        );
    }

    // Add Material
    @PostMapping
    public ResponseEntity<Material> addMaterial(@Valid @RequestBody Material material) {

        return new ResponseEntity<>(
                materialService.addMaterial(material),
                HttpStatus.CREATED
        );
    }

    // Update Material
    @PutMapping("/{id}")
    public ResponseEntity<Material> updateMaterial(
            @PathVariable String id,
            @Valid @RequestBody Material material) {

        return ResponseEntity.ok(
                materialService.updateMaterial(id, material)
        );
    }

    // Delete Material
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable String id) {

        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }

    // Get By Category
    @GetMapping("/category/{category}")
    public List<Material> getByCategory(@PathVariable String category) {

        return materialService.getByCategory(category);
    }

    // Get By Brand
    @GetMapping("/brand/{brand}")
    public List<Material> getByBrand(@PathVariable String brand) {

        return materialService.getByBrand(brand);
    }

    // Search Materials
    @GetMapping("/search")
    public List<Material> search(@RequestParam String keyword) {

        return materialService.searchMaterials(keyword);
    }

    // Featured Materials
    @GetMapping("/featured")
    public List<Material> getFeaturedMaterials() {

        return materialService.getFeaturedMaterials();
    }

    // Low Stock
    @GetMapping("/low-stock")
    public List<Material> lowStock() {

        return materialService.getLowStock();
    }

    // Update Stock
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Material> updateStock(
            @PathVariable String id,
            @RequestBody StockUpdateRequest request) {

        return ResponseEntity.ok(
                materialService.updateStock(id, request.getStock())
        );
    }

    // Bulk Stock Update
    @PatchMapping("/bulk-stock")
    public ResponseEntity<List<Material>> bulkUpdateStock(
            @RequestBody List<BulkStockUpdateRequest> requests) {

        return ResponseEntity.ok(
                materialService.bulkUpdateStock(requests)
        );
    }

    // Update Price
    @PatchMapping("/{id}/price")
    public ResponseEntity<Material> updatePrice(
            @PathVariable String id,
            @RequestBody PriceUpdateRequest request) {

        return ResponseEntity.ok(
                materialService.updatePrice(id, request.getPrice())
        );
    }

    // Upload Material Image
    @PostMapping("/{id}/image")
    public ResponseEntity<MaterialImage> uploadImage(
            @PathVariable String id,
            @RequestBody MaterialImageRequest request) {

        MaterialImage image = new MaterialImage();

        image.setMaterialId(id);
        image.setFileName(request.getFileName());
        image.setImageUrl(request.getImageUrl());

        return new ResponseEntity<>(
                materialImageService.saveImage(image),
                HttpStatus.CREATED
        );
    }

    // Get Images By Material ID
    @GetMapping("/{id}/image")
    public List<MaterialImage> getImages(@PathVariable String id) {

        return materialImageService.getImagesByMaterialId(id);
    }

}