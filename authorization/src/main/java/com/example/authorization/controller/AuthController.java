package com.example.authorization.controller;

import com.example.authorization.dto.*;
import com.example.authorization.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "🔐 Аутентификация", description = "API для регистрации, входа и управления токенами")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "📝 Регистрация нового пользователя",
            description = "Создаёт нового пользователя с ролью USER по умолчанию"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная регистрация",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "User registered successfully!"))),
            @ApiResponse(responseCode = "400", description = "Логин или email уже существует")
    })
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request,
                                           HttpServletRequest httpRequest) {
        log.info("Register request for login: {}", request.getLogin());
        return ResponseEntity.ok(authService.register(request, httpRequest));
    }

    @PostMapping("/login")
    @Operation(
            summary = "🔑 Вход в систему",
            description = """
            Аутентификация пользователя и выдача пары токенов:
            - **Access Token** — для доступа к API (живёт 15 минут)
            - **Refresh Token** — для обновления access токена (живёт 30 дней)
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        log.info("Login request for login: {}", request.getLogin());
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "🔄 Обновление токенов",
            description = """
            Обновляет пару токенов с использованием Refresh Token.
            
            **Важно:** Refresh Token можно использовать только один раз!
            После использования старый refresh token становится недействительным,
            а вы получаете новую пару токенов.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токены успешно обновлены",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный или просроченный refresh token")
    })
    public ResponseEntity<LoginResponse> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh токен из предыдущего логина",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
            )
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh request");
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "🚪 Выход из системы",
            description = "Инвалидирует access токен (добавляет в черный список). Токен больше не сможет быть использован."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный выход"),
            @ApiResponse(responseCode = "400", description = "Отсутствует заголовок Authorization"),
            @ApiResponse(responseCode = "401", description = "Невалидный токен")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> logout(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        log.info("Logout request");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("Logout failed: missing or invalid Authorization header");
            return ResponseEntity.badRequest().body("Missing or invalid Authorization header");
        }

        String token = authorization.substring(7);
        authService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }
}