package com.realconstruction.payment.model;

public class MonthlyReport {

    private String month;
    private double revenue;
    private long totalPayments;

    public MonthlyReport() {
    }

    public MonthlyReport(String month,
                         double revenue,
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

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }
}