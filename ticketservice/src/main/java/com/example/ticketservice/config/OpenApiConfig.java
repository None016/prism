package com.example.ticketservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Prism - Ticket Service API")
                        .version("1.0")
                        .description("""
                            REST API для управления заявками (тикетами).
                            """));
    }

    @Bean
    public OpenApiCustomizer schemaCustomizer() {
        return openApi -> {
            var ticketRequestSchema = openApi.getComponents().getSchemas().get("TicketRequest");
            if (ticketRequestSchema != null) {
                ticketRequestSchema.getProperties().remove("timeRequest");
            }
        };
    }
}