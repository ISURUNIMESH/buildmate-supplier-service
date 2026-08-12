package com.example.order_inventory_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderResponse {
    private String id;
    private String userId;
    private String status;
    private BigDecimal totalPrice;
    private Instant createdDate;
    private Instant updatedDate;
    private List<OrderItemResponse> items;

    @Data
    public static class OrderItemResponse {
        private String materialId;
        private Integer quantity;
        private BigDecimal price;
    }
}
