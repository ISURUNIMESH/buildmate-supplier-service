package com.buildmate.orderinventory.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "ApiKeyAuth";

    @Bean
    public OpenAPI orderInventoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BuildMate Order Inventory Service API")
                        .description(
                                "Order, cart, and inventory APIs protected by X-API-KEY. "
                                        + "Path and body IDs are MongoDB/backend identifiers, not friendly display IDs "
                                        + "(for example not U_001 or C_001). "
                                        + "Integrates with RabbitMQ asynchronously: publishes OrderCreatedEvent "
                                        + "after order creation and consumes PaymentCompletedEvent to update order payment status. "
                                        + "Broker credentials are supplied via environment configuration and are not part of this API.")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-KEY")
                                .description("BuildMate Order Inventory Service API key")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
