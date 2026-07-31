package com.construction.materialservice.service;

import com.construction.materialservice.model.Brand;
import com.construction.materialservice.repository.BrandRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public Brand addBrand(Brand brand) {

        if (brandRepository.existsByName(brand.getName())) {
            throw new RuntimeException("Brand already exists.");
        }

        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());

        return brandRepository.save(brand);
    }

    public Brand updateBrand(String id, Brand brand) {

        Brand existing = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found."));

        existing.setName(brand.getName());
        existing.setDescription(brand.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());

        return brandRepository.save(existing);
    }

    public void deleteBrand(String id) {

        if (!brandRepository.existsById(id)) {
            throw new RuntimeException("Brand not found.");
        }

        brandRepository.deleteById(id);
    }
}