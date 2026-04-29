package com.example.authorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление токенов")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token обязателен")
    @Schema(
            description = "Refresh токен (получается при логине)",
            example = "eyJhbGciOiJSUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsImp0aSI6IjA5ODA2Yjk0LTAxNzQtNGNkZS1hMTExLTE1ZjRlNTc4Njc5MCIsInN1YiI6InN0cmluZyIsImlhdCI6MTc3NzMxNjE5OSwiZXhwIjoxNzc3MzE3MDk5fQ.xxxx",
            required = true
    )
    private String refreshToken;
}