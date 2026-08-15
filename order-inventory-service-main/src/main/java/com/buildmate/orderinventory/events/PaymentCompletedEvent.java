package com.buildmate.orderinventory.events;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mirror of Payment Service PaymentCompletedEvent.
 * Consumed from payment.completed.queue (Phase 2).
 */
public class PaymentCompletedEvent {

    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private String paymentStatus;
    private Instant paidAt;

    public PaymentCompletedEvent() {
    }

    public PaymentCompletedEvent(
            String paymentId,
            String orderId,
            BigDecimal amount,
            String paymentStatus,
            Instant paidAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paidAt = paidAt;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    @Override
    public String toString() {
        return "PaymentCompletedEvent{"
                + "paymentId='" + paymentId + '\''
                + ", orderId='" + orderId + '\''
                + ", amount=" + amount
                + ", paymentStatus='" + paymentStatus + '\''
                + ", paidAt=" + paidAt
                + '}';
    }
}
