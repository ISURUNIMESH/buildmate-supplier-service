package com.buildmate.orderinventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Public service health check (no API key required)")
@SecurityRequirements
public class HealthController {
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns UP when the service is running. Excluded from API key filter.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service is up",
                    content = @Content(schema = @Schema(type = "string", example = "UP")))
    })
    public String health() { return "UP"; }
}
