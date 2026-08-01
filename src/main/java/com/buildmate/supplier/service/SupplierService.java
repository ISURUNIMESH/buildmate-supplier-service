package com.buildmate.supplier.service;

import com.buildmate.supplier.dto.SupplierLoginRequest;
import com.buildmate.supplier.dto.SupplierRegisterRequest;
import com.buildmate.supplier.dto.SupplierResponse;
import com.buildmate.supplier.dto.SupplierStatusUpdateRequest;
import com.buildmate.supplier.model.ApiKey;
import com.buildmate.supplier.model.Supplier;
import com.buildmate.supplier.model.SupplierStatus;
import com.buildmate.supplier.repository.ApiKeyRepository;
import com.buildmate.supplier.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);
    
    private final SupplierRepository supplierRepository;
    private final ApiKeyRepository apiKeyRepository;

    public SupplierService(SupplierRepository supplierRepository, ApiKeyRepository apiKeyRepository) {
        this.supplierRepository = supplierRepository;
        this.apiKeyRepository = apiKeyRepository;
    }

    public void validateApiKey(String apiKey) {
        ApiKey key = apiKeyRepository.findByKeyValue(apiKey).orElse(null);
        if (key == null) {
            logger.error("Invalid API Key: {}", apiKey);
            throw new RuntimeException("Invalid API Key");
        }
        if (key.getActive() == null || !key.getActive()) {
            logger.error("Inactive API Key: {}", apiKey);
            throw new RuntimeException("Invalid API Key");
        }
        logger.info("API key validated successfully: {}", key.getClientName());
    }

    public SupplierResponse registerSupplier(SupplierRegisterRequest request) {
        logger.info("Supplier registration started for email: {}", request.getEmail());
        if (supplierRepository.existsByEmail(request.getEmail())) {
            logger.warn("Duplicate email registration attempt: {}", request.getEmail());
            throw new RuntimeException("Email is already registered");
        }
        if (supplierRepository.existsBySupplierCode(request.getSupplierCode())) {
            logger.warn("Duplicate supplier code registration attempt: {}", request.getSupplierCode());
            throw new RuntimeException("Supplier code already exists");
        }
        if (supplierRepository.existsByBusinessRegistrationNo(request.getBusinessRegistrationNo())) {
            logger.warn("Duplicate business registration attempt: {}", request.getBusinessRegistrationNo());
            throw new RuntimeException("Business registration number already exists");
        }

        Supplier supplier = new Supplier();
        supplier.setSupplierCode(request.getSupplierCode());
        supplier.setCompanyName(request.getCompanyName());
        supplier.setOwnerName(request.getOwnerName());
        supplier.setEmail(request.getEmail());
        supplier.setPassword(request.getPassword());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setDistrict(request.getDistrict());
        supplier.setBusinessRegistrationNo(request.getBusinessRegistrationNo());
        supplier.setRating(0.0);
        supplier.setStatus(SupplierStatus.PENDING);
        supplier.setCreatedAt(LocalDateTime.now());
        supplier.setUpdatedAt(LocalDateTime.now());

        Supplier savedSupplier = supplierRepository.save(supplier);
        logger.info("Supplier registered successfully: {}", savedSupplier.getId());
        return mapToResponse(savedSupplier);
    }

    public SupplierResponse loginSupplier(SupplierLoginRequest request) {
        logger.info("Supplier login started for email: {}", request.getEmail());
        Supplier supplier = supplierRepository.findByEmail(request.getEmail()).orElse(null);
        if (supplier == null) {
            logger.warn("Missing supplier for login attempt: {}", request.getEmail());
            throw new RuntimeException("Supplier not found with email: " + request.getEmail());
        }

        if (!supplier.getPassword().equals(request.getPassword())) {
            logger.warn("Invalid password for login attempt: {}", request.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        logger.info("Supplier login successful: {}", supplier.getId());
        return mapToResponse(supplier);
    }

    public List<SupplierResponse> getAllSuppliers() {
        logger.info("Fetching all suppliers");
        return supplierRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SupplierResponse getSupplierById(String id) {
        logger.info("Fetching supplier: {}", id);
        Supplier supplier = supplierRepository.findById(id).orElse(null);
        if (supplier == null) {
            logger.error("Supplier not found: {}", id);
            throw new RuntimeException("Supplier not found with id: " + id);
        }
        return mapToResponse(supplier);
    }

    public SupplierResponse updateSupplier(String id, SupplierRegisterRequest request) {
        logger.info("Updating supplier: {}", id);
        Supplier supplier = supplierRepository.findById(id).orElse(null);
        if (supplier == null) {
            logger.error("Supplier not found for update: {}", id);
            throw new RuntimeException("Supplier not found with id: " + id);
        }

        supplier.setCompanyName(request.getCompanyName());
        supplier.setOwnerName(request.getOwnerName());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setDistrict(request.getDistrict());
        supplier.setUpdatedAt(LocalDateTime.now());

        Supplier updatedSupplier = supplierRepository.save(supplier);
        logger.info("Supplier updated successfully: {}", id);
        return mapToResponse(updatedSupplier);
    }

    public void deleteSupplier(String id) {
        logger.warn("Delete request received for supplier: {}", id);
        if (!supplierRepository.existsById(id)) {
            logger.error("Missing supplier for deletion: {}", id);
            throw new RuntimeException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
        logger.info("Supplier deleted successfully: {}", id);
    }

    public SupplierResponse updateSupplierStatus(String id, SupplierStatusUpdateRequest request) {
        logger.info("Updating status for supplier: {}", id);
        Supplier supplier = supplierRepository.findById(id).orElse(null);
        if (supplier == null) {
            logger.error("Missing supplier for status update: {}", id);
            throw new RuntimeException("Supplier not found with id: " + id);
        }

        supplier.setStatus(request.getStatus());
        supplier.setUpdatedAt(LocalDateTime.now());

        Supplier updatedSupplier = supplierRepository.save(supplier);
        logger.info("Supplier status updated successfully: {}", id);
        return mapToResponse(updatedSupplier);
    }

    public List<SupplierResponse> getSuppliersByDistrict(String district) {
        logger.info("Fetching suppliers for district: {}", district);
        return supplierRepository.findByDistrict(district)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SupplierResponse> getTopRatedSuppliers() {
        logger.info("Fetching top-rated suppliers");
        return supplierRepository.findByRatingGreaterThanEqual(4.0)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .supplierCode(supplier.getSupplierCode())
                .companyName(supplier.getCompanyName())
                .ownerName(supplier.getOwnerName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .district(supplier.getDistrict())
                .businessRegistrationNo(supplier.getBusinessRegistrationNo())
                .rating(supplier.getRating())
                .status(supplier.getStatus())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
