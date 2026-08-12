package com.example.order_inventory_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String userId;
    private String status;
    private BigDecimal totalPrice;
    private Instant createdDate;
    private Instant updatedDate;
    private List<OrderItem> items;

    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELLED,
        DELIVERED
    }

    @Data
    public static class OrderItem {
        private String materialId;
        private Integer quantity;
        private BigDecimal price;
    }
}
