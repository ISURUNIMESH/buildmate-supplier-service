package com.buildmate.auth.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildmate.auth.config.OpenApiConfig;
import com.buildmate.auth.dto.AuthUserResponse;
import com.buildmate.auth.entity.User;
import com.buildmate.auth.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Auth Server health and JWT-backed user resource endpoints")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Public liveness probe for the auth-server. No authentication required.",
            security = {})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Service is up",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "auth-server");
    }

    /**
     * Returns the authenticated BuildMate user derived from the JWT subject.
     * Gateway can route /api/auth/** here if needed; for now Auth Server resource usage is optional.
     */
    @GetMapping("/me")
    @Operation(
            summary = "Current user",
            description = "Returns the authenticated BuildMate user derived from the JWT subject claim.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT_SCHEME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authenticated user profile",
                    content = @Content(schema = @Schema(implementation = AuthUserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content)
    })
    public ResponseEntity<AuthUserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.requireById(jwt.getSubject());
        return ResponseEntity.ok(toResponse(user));
    }

    /**
     * Lists all registered users. Restricted to admins ({@code ROLE_ADMIN} / {@code ADMIN}).
     */
    @GetMapping("/users")
    @Operation(
            summary = "List users (admin)",
            description = "Lists all registered users. Requires a valid JWT with ROLE_ADMIN or ADMIN in the roles claim.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT_SCHEME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AuthUserResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated but missing admin role (ROLE_ADMIN / ADMIN)",
                    content = @Content)
    })
    public ResponseEntity<?> listUsers(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(403).body(Map.of(
                    "status", 403,
                    "error", "Forbidden",
                    "message", "Admin role required to list users"
            ));
        }
        List<AuthUserResponse> users = userService.listAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    private AuthUserResponse toResponse(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getProfileImageUrl(),
                user.getRoles() != null ? user.getRoles() : List.of("ROLE_USER")
        );
    }

    private boolean isAdmin(Jwt jwt) {
        Object rolesClaim = jwt.getClaim("roles");
        if (rolesClaim instanceof Collection<?> roles) {
            for (Object role : roles) {
                if (role == null) {
                    continue;
                }
                String normalized = String.valueOf(role).trim().toUpperCase().replace("ROLE_", "");
                if ("ADMIN".equals(normalized)) {
                    return true;
                }
            }
        }
        return false;
    }
}
