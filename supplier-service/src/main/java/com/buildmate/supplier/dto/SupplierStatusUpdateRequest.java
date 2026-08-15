package com.buildmate.supplier.dto;

import com.buildmate.supplier.model.SupplierStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SupplierStatusUpdateRequest", description = "Supplier status change payload")
public class SupplierStatusUpdateRequest {
    @NotNull
    @Schema(description = "New supplier status", example = "APPROVED")
    private SupplierStatus status;
}
