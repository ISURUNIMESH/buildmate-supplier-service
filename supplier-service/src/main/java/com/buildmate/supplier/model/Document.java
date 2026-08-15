package com.buildmate.supplier.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.mongodb.core.mapping.Document(collection = "supplier_documents")
public class Document {
    @Id
    private String id;

    @NotBlank(message = "Supplier ID is required")
    private String supplierId;

    @NotBlank(message = "Document name is required")
    @Size(max = 150, message = "Document name must not exceed 150 characters")
    private String documentName;

    @NotBlank(message = "Document type is required")
    @Size(max = 100, message = "Document type must not exceed 100 characters")
    private String documentType;

    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path must not exceed 500 characters")
    private String filePath;

    @NotNull(message = "Uploaded date and time is required")
    private LocalDateTime uploadedAt;
}
