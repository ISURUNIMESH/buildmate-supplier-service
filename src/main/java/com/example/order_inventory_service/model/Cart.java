package com.example.order_inventory_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "cart")
public class Cart {
    @Id
    private String id;
    private String userId;
    private List<CartItem> items = new ArrayList<>();

    @Data
    public static class CartItem {
        private String materialId;
        private Integer quantity;
        private java.math.BigDecimal price;
    }
}
