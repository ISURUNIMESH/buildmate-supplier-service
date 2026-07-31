package com.construction.materialservice.service;

import com.construction.materialservice.model.Material;
import com.construction.materialservice.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @InjectMocks
    private MaterialService materialService;

    private Material material;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        material = new Material();
        material.setId("1");
        material.setName("Cement");
        material.setDescription("Portland Cement");
        material.setCategory("Cement");
        material.setBrand("UltraTech");
        material.setPrice(2450.0);
        material.setStock(100);
        material.setUnit("Bag");
        material.setSupplierId("SUP001");
        material.setFeatured(false);
    }

    @Test
    void testAddMaterial() {

        when(materialRepository.save(any(Material.class))).thenReturn(material);

        Material saved = materialService.addMaterial(material);

        assertEquals("Cement", saved.getName());

        verify(materialRepository, times(1)).save(any(Material.class));
    }

    @Test
    void testGetMaterialById() {

        when(materialRepository.findById("1"))
                .thenReturn(Optional.of(material));

        Material result = materialService.getMaterialById("1");

        assertNotNull(result);

        assertEquals("Cement", result.getName());
    }

    @Test
    void testGetAllMaterials() {

        when(materialRepository.findAll())
                .thenReturn(List.of(material));

        List<Material> list = materialService.getAllMaterials();

        assertEquals(1, list.size());
    }

    @Test
    void testDeleteMaterial() {

        doNothing().when(materialRepository).deleteById("1");

        materialService.deleteMaterial("1");

        verify(materialRepository, times(1)).deleteById("1");
    }
}