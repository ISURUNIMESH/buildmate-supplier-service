package com.example.order_inventory_service.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReserveInventoryRequest {
    @Positive
    private Integer quantity;
}
