package com.buildmate.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
    @NotBlank
    private String supplierId;
    
    @NotBlank
    private String documentName;
    
    @NotBlank
    private String documentType;
    
    @NotBlank
    private String filePath;
}
