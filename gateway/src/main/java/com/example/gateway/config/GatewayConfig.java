package com.example.gateway.config;

import com.example.gateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service (без JWT)
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("http://localhost:8081"))

                // User Service (с JWT)
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("http://localhost:8081"))

                // Ticket Service
                .route("ticket-service", r -> r
                        .path("/api/tickets/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("http://localhost:8082"))

                // Workflow Service
                .route("workflow-service", r -> r
                        .path("/api/workflow/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("http://localhost:8083"))

                // Document Service
                .route("document-service", r -> r
                        .path("/api/documents/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("http://localhost:8084"))

                .build();
    }
}