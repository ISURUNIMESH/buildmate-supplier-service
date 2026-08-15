package com.buildmate.material.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String API_KEY_SCHEME = "ApiKeyAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-KEY")
                .description("BuildMate Material Service API key");

        return new OpenAPI()
                .info(new Info()
                        .title("BuildMate Material Service API")
                        .description("Material catalog, category, and brand APIs protected by X-API-KEY")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME, securityScheme))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME));
    }
}
