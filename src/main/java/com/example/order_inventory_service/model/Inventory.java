package com.example.order_inventory_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "inventory")
public class Inventory {
    @Id
    private String id;
    private String materialId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer minimumStock;
}
