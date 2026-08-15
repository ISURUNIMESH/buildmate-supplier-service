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
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "api_keys")
public class ApiKey {
    @Id
    private String id;

    @NotBlank(message = "API key value is required")
    @Size(max = 255, message = "API key value must not exceed 255 characters")
    private String keyValue;

    @NotBlank(message = "Client name is required")
    @Size(max = 150, message = "Client name must not exceed 150 characters")
    private String clientName;

    @NotNull(message = "Active status is required")
    private Boolean active;

    @NotNull(message = "Created date and time is required")
    private LocalDateTime createdAt;
}
