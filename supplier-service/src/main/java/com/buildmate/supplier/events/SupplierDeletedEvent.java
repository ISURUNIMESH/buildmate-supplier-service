package com.buildmate.supplier.events;

import java.time.Instant;

public class SupplierDeletedEvent {

    private String eventType;
    private String supplierId;
    private String supplierCode;
    private Instant occurredAt;

    public SupplierDeletedEvent() {
    }

    public SupplierDeletedEvent(String supplierId, String supplierCode, Instant occurredAt) {
        this.eventType = "SUPPLIER_DELETED";
        this.supplierId = supplierId;
        this.supplierCode = supplierCode;
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

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
