package com.buildmate.payment.exception;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ApiErrorResponse",
        description = "Validation error body returned by ValidationExceptionHandler"
)
public class ErrorResponse {

    @Schema(description = "Error timestamp", example = "2026-08-11T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "High-level error message", example = "Validation Failed")
    private String message;

    @Schema(description = "Field-level validation errors (field name to message)")
    private Map<String, String> errors;

    public ErrorResponse() {
    }

    public ErrorResponse(LocalDateTime timestamp, int status, String message, Map<String, String> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
