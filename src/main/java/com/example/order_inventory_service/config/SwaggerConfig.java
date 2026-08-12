package com.example.order_inventory_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI orderInventoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order & Inventory Service API")
                        .version("1.0")
                        .description("Order, inventory, cart, and event-driven workflow APIs"));
    }
}
