package com.example.order_inventory_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "order_history")
public class OrderHistory {
    @Id
    private String id;
    private String orderId;
    private String status;
    private Instant date;
    private String remarks;
}
