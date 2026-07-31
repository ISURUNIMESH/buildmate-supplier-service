package com.construction.materialservice.model;

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
public class Material {

    @Id
    private String id;

    @NotBlank(message = "Material name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    // Category
    @NotBlank(message = "Category is required")
    private String category;

    // Brand
    @NotBlank(message = "Brand is required")
    private String brand;

    // Price & Stock
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotBlank(message = "Unit is required")
    private String unit;

    // Supplier
    @NotBlank(message = "Supplier ID is required")
    private String supplierId;

    // Image
    private String imageUrl;

    // Featured Material
    private boolean featured;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}