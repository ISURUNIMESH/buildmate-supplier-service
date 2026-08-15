package com.buildmate.supplier.controller;

import com.buildmate.supplier.config.OpenApiConfig;
import com.buildmate.supplier.dto.ApiErrorResponse;
import com.buildmate.supplier.dto.SupplierLoginRequest;
import com.buildmate.supplier.dto.SupplierRatingUpdateRequest;
import com.buildmate.supplier.dto.SupplierRegisterRequest;
import com.buildmate.supplier.dto.SupplierResponse;
import com.buildmate.supplier.dto.SupplierReviewCreateRequest;
import com.buildmate.supplier.dto.SupplierReviewResponse;
import com.buildmate.supplier.dto.SupplierStatusUpdateRequest;
import com.buildmate.supplier.dto.SupplierUpdateRequest;
import com.buildmate.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier registration, profile, status, and reviews")
@SecurityRequirement(name = OpenApiConfig.API_KEY_SCHEME)
public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);
    private final SupplierService supplierService;

    @PostMapping
    @Operation(summary = "Register a supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supplier registered",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email, supplier code, or business registration already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> registerSupplier(
            @Valid @RequestBody SupplierRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.registerSupplier(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login a supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found for email",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> loginSupplier(
            @Valid @RequestBody SupplierLoginRequest request) {
        return ResponseEntity.ok(supplierService.loginSupplier(request));
    }

    @GetMapping
    @Operation(summary = "List all suppliers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suppliers returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SupplierResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/search")
    @Operation(summary = "Search suppliers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching suppliers returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SupplierResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Query parameter is blank",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<SupplierResponse>> searchSuppliers(
            @Parameter(description = "Search query", required = true) @RequestParam String query) {
        logger.info("Searching suppliers with query={}", query);
        return ResponseEntity.ok(supplierService.searchSuppliers(query));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "List top-rated suppliers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Top-rated suppliers returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SupplierResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<SupplierResponse>> getTopRatedSuppliers() {
        return ResponseEntity.ok(supplierService.getTopRatedSuppliers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> getSupplierById(
            @Parameter(description = "Supplier identifier", required = true) @PathVariable String id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> updateSupplier(
            @Parameter(description = "Supplier identifier", required = true) @PathVariable String id,
            @Valid @RequestBody SupplierUpdateRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supplier deleted"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteSupplier(
            @Parameter(description = "Supplier identifier", required = true) @PathVariable String id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update supplier status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> updateSupplierStatus(
            @Parameter(description = "Supplier identifier", required = true) @PathVariable String id,
            @Valid @RequestBody SupplierStatusUpdateRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplierStatus(id, request));
    }

    @PatchMapping("/{id}/verify")
    @Operation(summary = "Verify supplier", description = "Sets supplier status to APPROVED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier verified",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> verifySupplier(
            @Parameter(description = "Supplier identifier", required = true) @PathVariable String id) {
        logger.info("Verifying supplier: {}", id);
        return ResponseEntity.ok(supplierService.verifySupplier(id));
    }

    @PatchMapping("/{id}/rating")
    @Operation(summary = "Overwrite supplier rating")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rating updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> updateSupplierRating(
            @Parameter(description = "Supplier identifier", required = true) @PathVariable String id,
            @Valid @RequestBody SupplierRatingUpdateRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplierRating(id, request));
    }

    @PostMapping("/{id}/rating")
    @Operation(summary = "Add a supplier review", description = "Creates a review and recalculates average rating")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review added and average rating updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> addSupplierRating(
            @Parameter(description = "Supplier identifier", required = true) @PathVariable String id,
            @Valid @RequestBody SupplierReviewCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.addSupplierRating(id, request));
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "List supplier reviews")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SupplierReviewResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<SupplierReviewResponse>> getSupplierReviews(
            @Parameter(description = "Supplier identifier", required = true) @PathVariable String id) {
        return ResponseEntity.ok(supplierService.getSupplierReviews(id));
    }

    @GetMapping("/{id}/materials")
    @Operation(
            summary = "List materials for a supplier",
            description = "Loads catalog materials from Material Service via RestClient "
                    + "(GET /materials/supplier/{supplierId}). Does not store materials in Supplier Service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Materials returned from Material Service",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Map.class)))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing API key",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Material Service unavailable or unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<Map<String, Object>>> getSupplierMaterials(
            @Parameter(description = "Supplier identifier", required = true) @PathVariable String id) {
        return ResponseEntity.ok(supplierService.getSupplierMaterials(id));
    }
}
