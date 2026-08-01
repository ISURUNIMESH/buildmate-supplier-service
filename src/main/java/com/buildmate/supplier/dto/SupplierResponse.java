package com.buildmate.supplier.dto;

import com.buildmate.supplier.model.SupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private String id;
    private String supplierCode;
    private String companyName;
    private String ownerName;
    private String email;
    private String phone;
    private String address;
    private String district;
    private String businessRegistrationNo;
    private Double rating;
    private SupplierStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
