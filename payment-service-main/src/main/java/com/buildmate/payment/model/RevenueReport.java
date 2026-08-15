package com.buildmate.payment.model;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregate revenue report for successful payments")
public class RevenueReport {

    @Schema(description = "Total revenue from SUCCESS payments")
    private BigDecimal totalRevenue;

    @Schema(description = "Number of SUCCESS payments")
    private long totalPayments;

    public RevenueReport() {
    }

    public RevenueReport(BigDecimal totalRevenue, long totalPayments) {
        this.totalRevenue = totalRevenue;
        this.totalPayments = totalPayments;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }
}
