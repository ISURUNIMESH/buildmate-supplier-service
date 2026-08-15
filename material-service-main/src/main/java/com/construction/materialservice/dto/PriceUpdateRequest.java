package com.buildmate.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(name = "PriceUpdateRequest", description = "Price update payload")
public class PriceUpdateRequest {

    @NotNull
    @Positive
    @Schema(description = "New unit price", example = "1300.00")
    private Double price;

}
