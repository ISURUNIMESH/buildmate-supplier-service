package com.buildmate.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;

@Document(collection = "invoices")
@Schema(description = "Invoice record")
public class Invoice {

    @Id
    @Schema(description = "MongoDB invoice ID (backend ID)", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(description = "MongoDB/backend payment ID")
    private String paymentId;

    @Schema(description = "MongoDB/backend order ID")
    private String orderId;

    @Schema(description = "MongoDB/backend user ID")
    private String userId;

    @Schema(description = "Invoice amount")
    private BigDecimal amount;

    @Schema(description = "Invoice status")
    private String status;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    public Invoice() {
    }

    public Invoice(String paymentId,
                   String orderId,
                   String userId,
                   BigDecimal amount,
                   String status,
                   LocalDateTime createdAt) {

        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
