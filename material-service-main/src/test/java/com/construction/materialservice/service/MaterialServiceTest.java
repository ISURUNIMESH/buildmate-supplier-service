package com.buildmate.material.service;

import com.buildmate.material.dto.BulkStockUpdateRequest;
import com.buildmate.material.dto.MaterialImageRequest;
import com.buildmate.material.exception.ResourceNotFoundException;
import com.buildmate.material.model.Material;
import com.buildmate.material.model.MaterialImage;
import com.buildmate.material.producer.MaterialEventPublisher;
import com.buildmate.material.repository.MaterialImageRepository;
import com.buildmate.material.repository.MaterialRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialImageRepository materialImageRepository;

    @Mock
    private MaterialEventPublisher materialEventPublisher;

    @InjectMocks
    private MaterialService materialService;

    private Material material;

    @BeforeEach
    void setUp() {
        material = new Material();
        material.setId("1");
        material.setName("Cement");
        material.setDescription("Portland Cement");
        material.setCategory("Cement");
        material.setPrice(2450.0);
        material.setStock(100);
        material.setUnit("Bag");
        material.setSupplierId("SUP001");
        material.setBrand("Holcim");
        material.setFeatured(true);
    }

    @Test
    void testAddMaterial() {
        when(materialRepository.save(any(Material.class))).thenReturn(material);
        Material saved = materialService.addMaterial(material);
        assertEquals("Cement", saved.getName());
        verify(materialRepository, times(1)).save(any(Material.class));
        verify(materialEventPublisher).publishCreated(material);
    }

    @Test
    void testGetMaterialById() {
        when(materialRepository.findById("1")).thenReturn(Optional.of(material));
        Material result = materialService.getMaterialById("1");
        assertEquals("Cement", result.getName());
    }

    @Test
    void testGetAllMaterials() {
        when(materialRepository.findAll()).thenReturn(List.of(material));
        assertEquals(1, materialService.getAllMaterials().size());
    }

    @Test
    void testDeleteMaterial() {
        when(materialRepository.findById("1")).thenReturn(Optional.of(material));
        doNothing().when(materialRepository).deleteById("1");
        materialService.deleteMaterial("1");
        verify(materialRepository).deleteById("1");
        verify(materialEventPublisher).publishDeleted(material);
    }

    @Test
    void getFeaturedMaterials_returnsFeatured() {
        when(materialRepository.findByFeaturedTrue()).thenReturn(List.of(material));
        assertEquals(1, materialService.getFeaturedMaterials().size());
    }

    @Test
    void getByBrand_filtersBrand() {
        when(materialRepository.findByBrandIgnoreCase("Holcim")).thenReturn(List.of(material));
        assertEquals(1, materialService.getByBrand("Holcim").size());
    }

    @Test
    void getByBrand_blank_throws() {
        assertThrows(IllegalArgumentException.class, () -> materialService.getByBrand("  "));
    }

    @Test
    void getByCategory_ok() {
        when(materialRepository.findByCategory("Cement")).thenReturn(List.of(material));
        assertEquals(1, materialService.getByCategory("Cement").size());
    }

    @Test
    void bulkUpdateStock_updatesEach() {
        when(materialRepository.findById("1")).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));
        List<Material> result = materialService.bulkUpdateStock(
                List.of(new BulkStockUpdateRequest("1", 50)));
        assertEquals(50, result.get(0).getStock());
        verify(materialEventPublisher).publishStockUpdated(any(Material.class));
    }

    @Test
    void bulkUpdateStock_empty_throws() {
        assertThrows(IllegalArgumentException.class, () -> materialService.bulkUpdateStock(List.of()));
    }

    @Test
    void addMaterialImage_persistsMetadata() {
        when(materialRepository.findById("1")).thenReturn(Optional.of(material));
        when(materialImageRepository.save(any(MaterialImage.class))).thenAnswer(inv -> inv.getArgument(0));
        MaterialImage image = materialService.addMaterialImage(
                "1", new MaterialImageRequest("a.png", "https://cdn.example.com/a.png"));
        assertEquals("1", image.getMaterialId());
        assertEquals("https://cdn.example.com/a.png", image.getImageUrl());
    }

    @Test
    void addMaterialImage_missingMaterial_throws() {
        when(materialRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                materialService.addMaterialImage("missing",
                        new MaterialImageRequest("a.png", "https://cdn.example.com/a.png")));
    }
}
