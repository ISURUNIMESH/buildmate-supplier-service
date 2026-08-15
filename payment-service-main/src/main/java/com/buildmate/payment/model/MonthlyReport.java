package com.buildmate.payment.model;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Monthly revenue snapshot (current YearMonth)")
public class MonthlyReport {

    @Schema(description = "Month identifier (YearMonth)", example = "2026-08")
    private String month;

    @Schema(description = "Revenue for SUCCESS payments in the report scope")
    private BigDecimal revenue;

    @Schema(description = "Number of SUCCESS payments in the report scope")
    private long totalPayments;

    public MonthlyReport() {
    }

    public MonthlyReport(String month,
                         BigDecimal revenue,
                         long totalPayments) {

        this.month = month;
        this.revenue = revenue;
        this.totalPayments = totalPayments;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }
}
