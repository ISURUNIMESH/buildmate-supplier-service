package com.buildmate.supplier.dto;

import com.buildmate.supplier.model.SupplierStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SupplierResponse", description = "Supplier profile returned by the API (password excluded)")
public class SupplierResponse {
    @Schema(description = "Supplier identifier")
    private String id;

    @Schema(description = "Unique supplier code")
    private String supplierCode;

    @Schema(description = "Company name")
    private String companyName;

    @Schema(description = "Owner full name")
    private String ownerName;

    @Schema(description = "Email address")
    private String email;

    @Schema(description = "Contact phone")
    private String phone;

    @Schema(description = "Business address")
    private String address;

    @Schema(description = "District")
    private String district;

    @Schema(description = "Business registration number")
    private String businessRegistrationNo;

    @Schema(description = "Average rating")
    private Double rating;

    @Schema(description = "Approval status")
    private SupplierStatus status;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}
