package com.buildmate.supplier.dto;

import com.buildmate.supplier.model.SupplierStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierStatusUpdateRequest {
    @NotNull
    private SupplierStatus status;
}
