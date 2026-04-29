package com.example.authorization.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Auth Service API",
                version = "1.0.0",
                description = """
            ## Сервис аутентификации и авторизации
            
            ### 🔐 Аутентификация:
            - **Access Token** (Bearer) — для доступа к защищённым эндпоинтам
            - **Refresh Token** — только для обновления токенов
            
            ### 📋 Как получить токены:
            1. Выполните `POST /api/auth/login` с логином/паролем
            2. Скопируйте `accessToken` и `refreshToken` из ответа
            3. Нажмите кнопку **Authorize** и вставьте `Bearer {accessToken}`
            4. Для обновления токенов используйте `POST /api/auth/refresh` с `refreshToken` в теле
            """
        )
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Bearer Authentication")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                            🔑 Введите Access Token:
                            
                            **Формат:** `Bearer {ваш_access_token}`
                            
                            💡 *Токен можно получить через `/api/auth/login`*
                            """)
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}