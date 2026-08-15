package com.buildmate.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SupplierRatingUpdateRequest", description = "Direct rating overwrite payload")
public class SupplierRatingUpdateRequest {

    @NotNull
    @DecimalMin(value = "0.0", message = "Rating must be at least 0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5")
    @Schema(description = "New rating value (0–5)", example = "4.5")
    private Double rating;
}
