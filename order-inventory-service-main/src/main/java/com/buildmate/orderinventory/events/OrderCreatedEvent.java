package com.buildmate.orderinventory.events;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain event published when an order is successfully created and persisted.
 * Consumed asynchronously by Payment Service via RabbitMQ (Phase 1).
 */
public class OrderCreatedEvent {

    private String orderId;
    private String userId;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(
            String orderId,
            String userId,
            BigDecimal totalAmount,
            String status,
            Instant createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{"
                + "orderId='" + orderId + '\''
                + ", userId='" + userId + '\''
                + ", totalAmount=" + totalAmount
                + ", status='" + status + '\''
                + ", createdAt=" + createdAt
                + '}';
    }
}
