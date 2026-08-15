package com.buildmate.material.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "materials")
@Schema(name = "Material", description = "Construction material catalog item")
public class Material {

    @Id
    @Schema(description = "Material identifier", example = "66f1a2b3c4d5e6f7a8b9c0d1")
    private String id;

    @NotBlank(message = "Material name is required")
    @Schema(description = "Material name", example = "Portland Cement 50kg")
    private String name;

    @NotBlank(message = "Description is required")
    @Schema(description = "Material description", example = "General purpose Portland cement")
    private String description;

    // Category
    @NotBlank(message = "Category is required")
    @Schema(description = "Category name or id label", example = "Cement")
    private String category;

    // Price & Stock
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @Schema(description = "Unit price", example = "1250.00")
    private Double price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Schema(description = "Available stock quantity", example = "200")
    private Integer stock;

    @NotBlank(message = "Unit is required")
    @Schema(description = "Unit of measure", example = "bag")
    private String unit;

    // Supplier
    @NotBlank(message = "Supplier ID is required")
    @Schema(description = "Owning supplier identifier", example = "66f1a2b3c4d5e6f7a8b9c0d2")
    private String supplierId;

    /** Optional brand label for filtering (GET /materials/brand/{brand}). */
    @Schema(description = "Optional brand label used by brand filter", example = "Tokyo Super")
    private String brand;

    /** Featured flag for GET /materials/featured (default false when null). */
    @Schema(description = "Whether the material is featured", example = "false")
    private Boolean featured;

    // Timestamps
    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}
