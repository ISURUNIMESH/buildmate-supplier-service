package com.buildmate.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Info-only OpenAPI for the API Gateway. Does not aggregate downstream service specs.
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_JWT_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BuildMate API Gateway")
                        .version("1.0.0")
                        .description("""
                                BuildMate API Gateway is a reverse proxy (Spring Cloud Gateway WebFlux). \
                                It does not own business controllers; this document describes routed path \
                                prefixes and edge security only — OpenAPI is not aggregated from downstream services.

                                Routed path prefixes:
                                - /api/auth/**
                                - /api/suppliers/**
                                - /api/materials/**
                                - /api/categories/**
                                - /api/brands/**
                                - /api/payments/**
                                - /api/invoices/**
                                - /api/reports/**
                                - /api/orders/**
                                - /api/inventory/**
                                - /api/cart/**

                                Most routes require a JWT Bearer access token from the Auth Server. \
                                The gateway injects an X-API-KEY header to downstream services \
                                (configured via environment variables; never show real keys in docs — \
                                use a placeholder such as YOUR_DOWNSTREAM_API_KEY).
                                """))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT_SCHEME, new SecurityScheme()
                                .name(BEARER_JWT_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT required for most proxied routes. Example placeholder only: eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...."
                                )));
    }
}
