package com.example.ticketservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности.
 * Пока разрешаем все запросы, так как аутентификация будет на уровне API Gateway.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Отключаем CSRF для REST API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Без сессий
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем доступ к API, Swagger и Actuator
                        .requestMatchers("/api/v1/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/actuator/**")
                        .permitAll()
                        // Остальные запросы пока тоже разрешаем для удобства разработки
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}