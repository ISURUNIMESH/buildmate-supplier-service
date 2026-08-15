package com.buildmate.material.service;

import com.buildmate.material.model.MaterialImage;
import com.buildmate.material.repository.MaterialImageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaterialImageService {

    private final MaterialImageRepository materialImageRepository;

    public MaterialImageService(MaterialImageRepository materialImageRepository) {
        this.materialImageRepository = materialImageRepository;
    }

    // Save Image Details
    public MaterialImage saveImage(MaterialImage materialImage) {

        materialImage.setUploadedAt(LocalDateTime.now());

        return materialImageRepository.save(materialImage);
    }

    // Get Images By Material ID
    public List<MaterialImage> getImagesByMaterialId(String materialId) {

        return materialImageRepository.findByMaterialId(materialId);
    }
}
