package com.buildmate.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_JWT_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI authServerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BuildMate Auth Server API")
                        .version("1.0.0")
                        .description("""
                                BuildMate Auth Server exposes OAuth2/OIDC authorization endpoints \
                                (Google login, authorization code + PKCE, token, JWKS) and a JWT-protected \
                                resource API under /api/auth/**.

                                Obtain a JWT access token via the OAuth2/OIDC flow, then call resource \
                                endpoints with Authorization: Bearer <access_token>. \
                                Use placeholders only for Google client credentials and signing material — \
                                never paste real secrets into Swagger examples.
                                """))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT_SCHEME, new SecurityScheme()
                                .name(BEARER_JWT_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token from the BuildMate OAuth2/OIDC flow (placeholder: eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...)."
                                )));
    }
}
