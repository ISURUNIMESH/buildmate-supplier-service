package com.buildmate.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BulkStockUpdateRequest", description = "Single material stock change within a bulk update")
public class BulkStockUpdateRequest {

    @NotBlank(message = "Material id is required")
    @Schema(description = "Material identifier", example = "66f1a2b3c4d5e6f7a8b9c0d1")
    private String id;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Schema(description = "New stock quantity", example = "150")
    private Integer stock;
}
