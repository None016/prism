package com.example.workflow_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            // Получаем текущую аутентификацию из SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                // Извлекаем JWT токен
                String tokenValue = jwtAuth.getToken().getTokenValue();

                // Добавляем Authorization header
                template.header("Authorization", "Bearer " + tokenValue);
            }
        };
    }
}