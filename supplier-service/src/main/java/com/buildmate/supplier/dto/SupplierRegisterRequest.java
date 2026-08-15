package com.buildmate.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SupplierRegisterRequest", description = "New supplier registration payload")
public class SupplierRegisterRequest {
    @NotBlank
    @Schema(description = "Unique supplier code", example = "SUP-001")
    private String supplierCode;
    
    @NotBlank
    @Schema(description = "Company name", example = "BuildMart Traders")
    private String companyName;
    
    @NotBlank
    @Schema(description = "Owner full name", example = "Nimal Perera")
    private String ownerName;
    
    @NotBlank
    @Email
    @Schema(description = "Login email", example = "nimal@buildmart.lk")
    private String email;
    
    @NotBlank
    @Schema(description = "Login password", example = "Secret123!")
    private String password;
    
    @NotBlank
    @Schema(description = "Contact phone", example = "+94771234567")
    private String phone;
    
    @NotBlank
    @Schema(description = "Business address", example = "12 Galle Road, Colombo")
    private String address;
    
    @NotBlank
    @Schema(description = "District", example = "Colombo")
    private String district;
    
    @NotBlank
    @Schema(description = "Business registration number", example = "PV-123456")
    private String businessRegistrationNo;
}
