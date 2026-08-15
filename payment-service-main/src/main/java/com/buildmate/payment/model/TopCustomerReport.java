package com.buildmate.payment.model;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Top spending customer among SUCCESS payments")
public class TopCustomerReport {

    @Schema(description = "MongoDB/backend user ID of the top customer")
    private String userId;

    @Schema(description = "Total amount spent by the top customer")
    private BigDecimal totalSpent;

    @Schema(description = "Number of SUCCESS payments by the top customer")
    private long totalPayments;

    public TopCustomerReport() {
    }

    public TopCustomerReport(String userId,
                             BigDecimal totalSpent,
                             long totalPayments) {
        this.userId = userId;
        this.totalSpent = totalSpent;
        this.totalPayments = totalPayments;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }
}
