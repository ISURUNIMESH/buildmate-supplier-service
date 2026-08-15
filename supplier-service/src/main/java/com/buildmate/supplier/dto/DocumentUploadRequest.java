package com.buildmate.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "DocumentUploadRequest",
        description = "JSON metadata for a supplier document (file path/URL reference, not multipart upload)")
public class DocumentUploadRequest {
    @NotBlank
    @Schema(description = "Supplier identifier (also taken from path); set from path by the controller", example = "66f1a2b3c4d5e6f7a8b9c0d2")
    private String supplierId;
    
    @NotBlank
    @Schema(description = "Document display name", example = "Business Registration.pdf")
    private String documentName;
    
    @NotBlank
    @Schema(description = "Document type label", example = "BUSINESS_REGISTRATION")
    private String documentType;
    
    @NotBlank
    @Schema(description = "Stored file path or URL", example = "https://cdn.example.com/docs/br-001.pdf")
    private String filePath;
}
