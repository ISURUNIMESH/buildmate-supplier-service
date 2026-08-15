package com.buildmate.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "StockUpdateRequest", description = "Stock quantity update payload")
public class StockUpdateRequest {

    @NotNull
    @Min(0)
    @Schema(description = "New stock quantity", example = "100")
    private Integer stock;

}
