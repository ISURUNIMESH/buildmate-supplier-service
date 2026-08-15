package com.buildmate.supplier.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "supplier_reviews")
public class SupplierReview {

    @Id
    private String id;

    @NotBlank
    private String supplierId;

    @Size(max = 150)
    private String reviewerName;

    @Size(max = 1000)
    private String comment;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private Double rating;

    @NotNull
    private LocalDateTime createdAt;
}
