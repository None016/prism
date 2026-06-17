// authorization/controller/UserInfoController.java
package com.example.authorization.controller;

import com.example.authorization.dto.UserInfoResponse;
import com.example.authorization.model.Users;
import com.example.authorization.repository.UsersRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "👤 Информация о пользователе", description = "API для получения информации о пользователе")
@SecurityRequirement(name = "Bearer Authentication")
public class UserInfoController {

    private final UsersRepository usersRepository;

    @GetMapping("/me")
    @Operation(summary = "Получить информацию о текущем пользователе")
    public ResponseEntity<UserInfoResponse> getCurrentUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("Getting user info for: {}", username);

        // Используем метод с JOIN FETCH для загрузки организаций
        Users user = usersRepository.findByLoginWithContractors(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return ResponseEntity.ok(UserInfoResponse.fromEntity(user));
    }
}