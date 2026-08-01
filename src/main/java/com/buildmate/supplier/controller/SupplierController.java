package com.buildmate.supplier.controller;

import com.buildmate.supplier.dto.SupplierLoginRequest;
import com.buildmate.supplier.dto.SupplierRegisterRequest;
import com.buildmate.supplier.dto.SupplierResponse;
import com.buildmate.supplier.dto.SupplierStatusUpdateRequest;
import com.buildmate.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);
    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponse> registerSupplier(
            @RequestHeader("X-API-KEY") String apiKey,
            @Valid @RequestBody SupplierRegisterRequest request) {
        logger.info("Received request to register supplier: {}", request.getEmail());
        supplierService.validateApiKey(apiKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.registerSupplier(request));
    }

    @PostMapping("/login")
    public ResponseEntity<SupplierResponse> loginSupplier(
            @RequestHeader("X-API-KEY") String apiKey,
            @Valid @RequestBody SupplierLoginRequest request) {
        logger.info("Received request to login supplier: {}", request.getEmail());
        supplierService.validateApiKey(apiKey);
        return ResponseEntity.ok(supplierService.loginSupplier(request));
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers(
            @RequestHeader("X-API-KEY") String apiKey) {
        logger.info("Received request to fetch all suppliers");
        supplierService.validateApiKey(apiKey);
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @RequestHeader("X-API-KEY") String apiKey,
            @PathVariable String id) {
        logger.info("Received request to fetch supplier by id: {}", id);
        supplierService.validateApiKey(apiKey);
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @RequestHeader("X-API-KEY") String apiKey,
            @PathVariable String id,
            @Valid @RequestBody SupplierRegisterRequest request) {
        logger.info("Received request to update supplier: {}", id);
        supplierService.validateApiKey(apiKey);
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(
            @RequestHeader("X-API-KEY") String apiKey,
            @PathVariable String id) {
        logger.info("Received request to delete supplier: {}", id);
        supplierService.validateApiKey(apiKey);
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SupplierResponse> updateSupplierStatus(
            @RequestHeader("X-API-KEY") String apiKey,
            @PathVariable String id,
            @Valid @RequestBody SupplierStatusUpdateRequest request) {
        logger.info("Received request to update supplier status: {}", id);
        supplierService.validateApiKey(apiKey);
        return ResponseEntity.ok(supplierService.updateSupplierStatus(id, request));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<SupplierResponse>> getTopRatedSuppliers(
            @RequestHeader("X-API-KEY") String apiKey) {
        logger.info("Received request to fetch top-rated suppliers");
        supplierService.validateApiKey(apiKey);
        return ResponseEntity.ok(supplierService.getTopRatedSuppliers());
    }
}
