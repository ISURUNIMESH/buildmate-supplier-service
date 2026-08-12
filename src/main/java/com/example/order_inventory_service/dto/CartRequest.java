package com.example.order_inventory_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartRequest {
    @NotBlank
    private String userId;

    @NotBlank
    private String materialId;

    @Positive
    private Integer quantity;

    @Positive
    private BigDecimal price;
}
