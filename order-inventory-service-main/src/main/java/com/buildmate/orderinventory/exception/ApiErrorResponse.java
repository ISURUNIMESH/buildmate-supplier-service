package com.buildmate.orderinventory.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(
        name = "ApiErrorResponse",
        description = "Error body returned by GlobalExceptionHandler "
                + "(validation responses include details; other errors typically omit details)"
)
public class ApiErrorResponse {

    @Schema(description = "Error timestamp as ISO-8601 instant string", example = "2026-08-11T05:00:00Z")
    private String timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error summary or message", example = "Validation failed")
    private String error;

    @Schema(description = "Field-level details for validation failures (field name to message)")
    private Map<String, String> details;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}
