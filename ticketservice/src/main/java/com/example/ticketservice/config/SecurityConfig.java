package com.example.ticketservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Включаем поддержку @PreAuthorize
public class SecurityConfig {

    // Конфигурация для публичных эндпоинтов (основное API)
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/tickets/**", "/api/v1/debug/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // Все запросы к основному API разрешены
                );

        return http.build();
    }

    // Конфигурация для internal эндпоинтов (без проверок авторизации)
    @Bean
    @Order(2)
    public SecurityFilterChain internalSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/internal/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // Все internal запросы разрешены
                );

        return http.build();
    }

    // Конфигурация для остальных эндпоинтов (Swagger, Actuator)
    @Bean
    @Order(3)
    public SecurityFilterChain otherSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/actuator/**")
                        .permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}