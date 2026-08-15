package com.buildmate.supplier.config;

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
    public OpenAPI supplierOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BuildMate Supplier Service API")
                        .description("Supplier management, reviews, and document APIs protected by X-API-KEY")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME, new SecurityScheme()
                                .name("X-API-KEY")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("BuildMate Supplier Service API key")))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME));
    }
}
