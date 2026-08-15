package com.buildmate.orderinventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "Quantity payload for inventory reserve/release")
public class ReserveInventoryRequest {
    @Positive
    @Schema(description = "Quantity to reserve or release", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer quantity;
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
