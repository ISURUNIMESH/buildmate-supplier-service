package com.buildmate.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SupplierReviewCreateRequest", description = "Create a review and recalculate average rating")
public class SupplierReviewCreateRequest {

    @NotNull
    @DecimalMin(value = "0.0", message = "Rating must be at least 0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5")
    @Schema(description = "Review rating (0–5)", example = "4.0")
    private Double rating;

    @Size(max = 150)
    @Schema(description = "Optional reviewer display name", example = "Kamal Silva")
    private String reviewerName;

    @Size(max = 1000)
    @Schema(description = "Optional review comment", example = "Reliable delivery and good quality.")
    private String comment;
}
