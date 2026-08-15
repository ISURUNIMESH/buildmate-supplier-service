package com.buildmate.supplier.events;

import java.time.Instant;

/**
 * Generic supplier lifecycle event (created / updated).
 */
public class SupplierEvent {

    private String eventType;
    private String supplierId;
    private String supplierCode;
    private String companyName;
    private String email;
    private String status;
    private Instant occurredAt;

    public SupplierEvent() {
    }

    public SupplierEvent(
            String eventType,
            String supplierId,
            String supplierCode,
            String companyName,
            String email,
            String status,
            Instant occurredAt) {
        this.eventType = eventType;
        this.supplierId = supplierId;
        this.supplierCode = supplierCode;
        this.companyName = companyName;
        this.email = email;
        this.status = status;
        this.occurredAt = occurredAt;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
