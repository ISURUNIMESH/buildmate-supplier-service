package com.buildmate.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirror of Order & Inventory OrderCreatedEvent.
 * Deserialized from RabbitMQ messages on order.created.queue (Phase 1 — log only).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private String orderId;
    private String userId;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;
}
