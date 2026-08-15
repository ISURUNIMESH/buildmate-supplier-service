package com.buildmate.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain event published when a payment reaches a completed state.
 * Consumed by Order & Inventory Service to mark the order PAID (Phase 2).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {

    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private String paymentStatus;
    private Instant paidAt;
}
