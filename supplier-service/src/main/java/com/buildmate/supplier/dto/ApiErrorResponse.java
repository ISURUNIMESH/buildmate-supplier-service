package com.buildmate.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error body shape produced by {@code GlobalExceptionHandler} and {@code ApiKeyFilter}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ApiErrorResponse", description = "Standard API error response")
public class ApiErrorResponse {

    @Schema(description = "ISO-8601 timestamp when the error occurred", example = "2026-08-11T05:21:00.123Z")
    private String timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private Integer status;

    @Schema(description = "HTTP reason phrase", example = "Bad Request")
    private String error;

    @Schema(description = "Human-readable error message", example = "Validation failed")
    private String message;

    @Schema(description = "Request path that produced the error", example = "/suppliers")
    private String path;

    @Schema(description = "Optional field-level validation details (validation errors only)")
    private Map<String, String> details;
}
