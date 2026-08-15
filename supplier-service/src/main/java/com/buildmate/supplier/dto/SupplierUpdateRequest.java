package com.buildmate.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partial update payload for {@code PUT /suppliers/{id}}.
 * Matches {@link com.buildmate.supplier.service.SupplierService#updateSupplier} mutable fields only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SupplierUpdateRequest", description = "Mutable supplier profile fields")
public class SupplierUpdateRequest {

    @NotBlank
    @Schema(description = "Company name", example = "BuildMart Traders")
    private String companyName;

    @NotBlank
    @Schema(description = "Owner full name", example = "Nimal Perera")
    private String ownerName;

    @NotBlank
    @Schema(description = "Contact phone", example = "+94771234567")
    private String phone;

    @NotBlank
    @Schema(description = "Business address", example = "12 Galle Road, Colombo")
    private String address;

    @NotBlank
    @Schema(description = "District", example = "Colombo")
    private String district;
}
