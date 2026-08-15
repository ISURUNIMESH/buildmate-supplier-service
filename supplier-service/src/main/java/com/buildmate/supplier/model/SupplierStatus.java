package com.buildmate.supplier.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SupplierStatus", description = "Supplier approval lifecycle status")
public enum SupplierStatus {
    PENDING,
    APPROVED,
    REJECTED
}
