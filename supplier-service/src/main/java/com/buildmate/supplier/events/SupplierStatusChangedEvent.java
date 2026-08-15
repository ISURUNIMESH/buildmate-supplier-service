package com.buildmate.supplier.events;

import java.time.Instant;

public class SupplierStatusChangedEvent {

    private String eventType;
    private String supplierId;
    private String supplierCode;
    private String previousStatus;
    private String newStatus;
    private Instant occurredAt;

    public SupplierStatusChangedEvent() {
    }

    public SupplierStatusChangedEvent(
            String supplierId,
            String supplierCode,
            String previousStatus,
            String newStatus,
            Instant occurredAt) {
        this.eventType = "SUPPLIER_STATUS_CHANGED";
        this.supplierId = supplierId;
        this.supplierCode = supplierCode;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
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

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
