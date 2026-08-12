package com.realconstruction.payment.model;

public class TopCustomerReport {

    private String userId;
    private double totalSpent;
    private long totalPayments;

    public TopCustomerReport() {
    }

    public TopCustomerReport(String userId,
                             double totalSpent,
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

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }
}