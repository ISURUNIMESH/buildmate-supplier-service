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
@Schema(name = "SupplierLoginRequest", description = "Supplier login credentials")
public class SupplierLoginRequest {
    @NotBlank
    @Email
    @Schema(description = "Registered email", example = "nimal@buildmart.lk")
    private String email;
    
    @NotBlank
    @Schema(description = "Password", example = "Secret123!")
    private String password;
}
