package com.buildmate.orderinventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Update order status request")
public class UpdateOrderStatusRequest {
    @NotNull
    @Schema(description = "New order status", example = "CONFIRMED")
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
