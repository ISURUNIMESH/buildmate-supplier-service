package com.buildmate.supplier.service;

import com.buildmate.supplier.client.MaterialServiceClient;
import com.buildmate.supplier.dto.SupplierLoginRequest;
import com.buildmate.supplier.dto.SupplierRatingUpdateRequest;
import com.buildmate.supplier.dto.SupplierRegisterRequest;
import com.buildmate.supplier.dto.SupplierResponse;
import com.buildmate.supplier.dto.SupplierReviewCreateRequest;
import com.buildmate.supplier.dto.SupplierReviewResponse;
import com.buildmate.supplier.dto.SupplierStatusUpdateRequest;
import com.buildmate.supplier.dto.SupplierUpdateRequest;
import com.buildmate.supplier.exception.DuplicateSupplierException;
import com.buildmate.supplier.exception.SupplierNotFoundException;
import com.buildmate.supplier.exception.UnauthorizedException;
import com.buildmate.supplier.model.Supplier;
import com.buildmate.supplier.model.SupplierReview;
import com.buildmate.supplier.model.SupplierStatus;
import com.buildmate.supplier.producer.SupplierEventPublisher;
import com.buildmate.supplier.repository.SupplierRepository;
import com.buildmate.supplier.repository.SupplierReviewRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);

    private final SupplierRepository supplierRepository;
    private final SupplierReviewRepository supplierReviewRepository;
    private final SupplierEventPublisher supplierEventPublisher;
    private final MaterialServiceClient materialServiceClient;

    public SupplierService(
            SupplierRepository supplierRepository,
            SupplierReviewRepository supplierReviewRepository,
            SupplierEventPublisher supplierEventPublisher,
            MaterialServiceClient materialServiceClient) {
        this.supplierRepository = supplierRepository;
        this.supplierReviewRepository = supplierReviewRepository;
        this.supplierEventPublisher = supplierEventPublisher;
        this.materialServiceClient = materialServiceClient;
    }

    public SupplierResponse registerSupplier(SupplierRegisterRequest request) {
        logger.info("Supplier registration started for email: {}", request.getEmail());
        if (supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateSupplierException("Email is already registered");
        }
        if (supplierRepository.existsBySupplierCode(request.getSupplierCode())) {
            throw new DuplicateSupplierException("Supplier code already exists");
        }
        if (supplierRepository.existsByBusinessRegistrationNo(request.getBusinessRegistrationNo())) {
            throw new DuplicateSupplierException("Business registration number already exists");
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
        supplierEventPublisher.publishCreated(savedSupplier);
        return mapToResponse(savedSupplier);
    }

    public SupplierResponse loginSupplier(SupplierLoginRequest request) {
        Supplier supplier = supplierRepository.findByEmail(request.getEmail()).orElse(null);
        if (supplier == null) {
            throw new SupplierNotFoundException("Supplier not found with email: " + request.getEmail());
        }
        if (!supplier.getPassword().equals(request.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return mapToResponse(supplier);
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public SupplierResponse getSupplierById(String id) {
        return mapToResponse(requireSupplier(id));
    }

    public SupplierResponse updateSupplier(String id, SupplierUpdateRequest request) {
        Supplier supplier = requireSupplier(id);
        supplier.setCompanyName(request.getCompanyName());
        supplier.setOwnerName(request.getOwnerName());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setDistrict(request.getDistrict());
        supplier.setUpdatedAt(LocalDateTime.now());
        Supplier updatedSupplier = supplierRepository.save(supplier);
        supplierEventPublisher.publishUpdated(updatedSupplier);
        return mapToResponse(updatedSupplier);
    }

    public void deleteSupplier(String id) {
        Supplier supplier = requireSupplier(id);
        String supplierCode = supplier.getSupplierCode();
        supplierRepository.deleteById(id);
        supplierEventPublisher.publishDeleted(id, supplierCode);
    }

    public SupplierResponse updateSupplierStatus(String id, SupplierStatusUpdateRequest request) {
        Supplier supplier = requireSupplier(id);
        String previousStatus = supplier.getStatus() != null ? supplier.getStatus().name() : null;
        supplier.setStatus(request.getStatus());
        supplier.setUpdatedAt(LocalDateTime.now());
        Supplier updatedSupplier = supplierRepository.save(supplier);
        supplierEventPublisher.publishStatusChanged(updatedSupplier, previousStatus);
        return mapToResponse(updatedSupplier);
    }

    public SupplierResponse verifySupplier(String id) {
        SupplierStatusUpdateRequest request = SupplierStatusUpdateRequest.builder()
                .status(SupplierStatus.APPROVED)
                .build();
        return updateSupplierStatus(id, request);
    }

    public SupplierResponse updateSupplierRating(String id, SupplierRatingUpdateRequest request) {
        Supplier supplier = requireSupplier(id);
        supplier.setRating(request.getRating());
        supplier.setUpdatedAt(LocalDateTime.now());
        Supplier updatedSupplier = supplierRepository.save(supplier);
        supplierEventPublisher.publishUpdated(updatedSupplier);
        return mapToResponse(updatedSupplier);
    }

    public SupplierResponse addSupplierRating(String id, SupplierReviewCreateRequest request) {
        Supplier supplier = requireSupplier(id);

        SupplierReview review = SupplierReview.builder()
                .supplierId(id)
                .reviewerName(request.getReviewerName())
                .comment(request.getComment())
                .rating(request.getRating())
                .createdAt(LocalDateTime.now())
                .build();
        supplierReviewRepository.save(review);

        List<SupplierReview> reviews = supplierReviewRepository.findBySupplierIdOrderByCreatedAtDesc(id);
        double avg = reviews.stream().mapToDouble(SupplierReview::getRating).average().orElse(request.getRating());
        supplier.setRating(Math.round(avg * 10.0) / 10.0);
        supplier.setUpdatedAt(LocalDateTime.now());
        Supplier saved = supplierRepository.save(supplier);
        supplierEventPublisher.publishUpdated(saved);
        return mapToResponse(saved);
    }

    public List<SupplierReviewResponse> getSupplierReviews(String id) {
        requireSupplier(id);
        return supplierReviewRepository.findBySupplierIdOrderByCreatedAtDesc(id).stream()
                .map(r -> SupplierReviewResponse.builder()
                        .id(r.getId())
                        .supplierId(r.getSupplierId())
                        .reviewerName(r.getReviewerName())
                        .comment(r.getComment())
                        .rating(r.getRating())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SupplierResponse> searchSuppliers(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query parameter is required");
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        Map<String, Supplier> matched = new LinkedHashMap<>();
        supplierRepository.findByCompanyNameContainingIgnoreCase(query.trim())
                .forEach(s -> matched.put(s.getId(), s));
        supplierRepository.findAll().stream()
                .filter(s -> containsIgnoreCase(s.getEmail(), q)
                        || containsIgnoreCase(s.getSupplierCode(), q)
                        || containsIgnoreCase(s.getOwnerName(), q)
                        || containsIgnoreCase(s.getDistrict(), q)
                        || containsIgnoreCase(s.getPhone(), q))
                .forEach(s -> matched.put(s.getId(), s));
        return matched.values().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSupplierMaterials(String id) {
        requireSupplier(id);
        return materialServiceClient.getMaterialsBySupplierId(id);
    }

    public List<SupplierResponse> getSuppliersByDistrict(String district) {
        return supplierRepository.findByDistrict(district).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SupplierResponse> getTopRatedSuppliers() {
        return supplierRepository.findTop10ByOrderByRatingDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Supplier requireSupplier(String id) {
        Supplier supplier = supplierRepository.findById(id).orElse(null);
        if (supplier == null) {
            throw new SupplierNotFoundException("Supplier not found with id: " + id);
        }
        return supplier;
    }

    private static boolean containsIgnoreCase(String value, String queryLower) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(queryLower);
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
