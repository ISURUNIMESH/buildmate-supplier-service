package com.realconstruction.payment.model;

public class RevenueReport {

    private double totalRevenue;
    private long totalPayments;

    public RevenueReport() {
    }

    public RevenueReport(double totalRevenue, long totalPayments) {
        this.totalRevenue = totalRevenue;
        this.totalPayments = totalPayments;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }
}