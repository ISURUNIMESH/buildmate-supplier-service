package com.buildmate.material.service;

import com.buildmate.material.exception.DuplicateResourceException;
import com.buildmate.material.exception.ResourceNotFoundException;
import com.buildmate.material.model.Brand;
import com.buildmate.material.repository.BrandRepository;
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
            throw new DuplicateResourceException("Brand already exists.");
        }

        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());

        return brandRepository.save(brand);
    }

    public Brand updateBrand(String id, Brand brand) {

        Brand existing = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found."));

        existing.setName(brand.getName());
        existing.setDescription(brand.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());

        return brandRepository.save(existing);
    }

    public void deleteBrand(String id) {

        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Brand not found.");
        }

        brandRepository.deleteById(id);
    }
}
