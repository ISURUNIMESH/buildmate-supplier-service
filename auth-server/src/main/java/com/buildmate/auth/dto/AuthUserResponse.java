package com.buildmate.auth.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthUserResponse", description = "BuildMate authenticated user profile returned by resource endpoints")
public record AuthUserResponse(
        @Schema(description = "Stable user id (JWT subject)", example = "507f1f77bcf86cd799439011")
        String id,
        @Schema(description = "User email", example = "user@example.com")
        String email,
        @Schema(description = "Display name", example = "Jane Doe")
        String name,
        @Schema(description = "Profile image URL", example = "https://example.com/avatar.png", nullable = true)
        String profileImageUrl,
        @Schema(description = "Assigned roles (e.g. ROLE_USER, ROLE_ADMIN)", example = "[\"ROLE_USER\"]")
        List<String> roles
) {
}
