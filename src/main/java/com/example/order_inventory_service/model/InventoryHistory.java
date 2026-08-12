package com.example.order_inventory_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "inventory_history")
public class InventoryHistory {
    @Id
    private String id;
    private String materialId;
    private String action;
    private Integer quantity;
    private Instant date;
    private String reference;
}
