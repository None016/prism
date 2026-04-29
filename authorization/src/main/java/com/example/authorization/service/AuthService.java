package com.example.authorization.service;

import com.example.authorization.dto.LoginRequest;
import com.example.authorization.dto.LoginResponse;
import com.example.authorization.dto.RefreshTokenRequest;
import com.example.authorization.dto.RegisterRequest;
import com.example.authorization.model.Role;
import com.example.authorization.model.Users;
import com.example.authorization.repository.RoleRepository;
import com.example.authorization.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistService blacklistService;

    @Transactional
    public String register(RegisterRequest request) {
        // Проверка на существование
        if (usersRepository.existsByLogin(request.getLogin())) {
            log.warn("Registration failed: login {} already exists", request.getLogin());
            return "Login already exists!";
        }

        if (usersRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email {} already exists", request.getEmail());
            return "Email already exists!";
        }

        // Получаем или создаём роль USER по умолчанию
        Role userRole = roleRepository.findByNameRole("ROLE_USER")
                .orElseGet(() -> {
                    log.info("Creating default ROLE_USER");
                    Role newRole = new Role();
                    newRole.setNameRole("ROLE_USER");
                    newRole.setNote("Default user role");
                    return roleRepository.save(newRole);
                });

        // Создаём пользователя
        Users user = Users.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .patronymic(request.getPatronymic())
                .phone(request.getPhone())
                .email(request.getEmail())
                .login(request.getLogin())
                .heshPassword(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .build();

        usersRepository.save(user);
        log.info("User registered successfully: {}", request.getLogin());

        return "User registered successfully!";
    }

    public LoginResponse login(LoginRequest request) {
        log.info("=== LOGIN START ===");
        log.info("Login attempt for user: {}", request.getLogin());

        try {
            log.info("Step 1: Authenticating...");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );
            log.info("Step 1: Authentication successful");

            log.info("Step 2: Loading user details...");
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());
            log.info("Step 2: User details loaded for: {}", userDetails.getUsername());

            log.info("Step 3: Generating access token...");
            String accessToken = jwtService.generateAccessToken(userDetails);
            log.info("Step 3: Access token generated");

            log.info("Step 4: Generating refresh token...");
            String refreshToken = jwtService.generateRefreshToken(userDetails);
            log.info("Step 4: Refresh token generated");

            log.info("Step 5: Saving refresh token to Redis...");
            refreshTokenService.saveRefreshToken(refreshToken, userDetails.getUsername());
            log.info("Step 5: Refresh token saved");

            log.info("Step 6: Building response...");
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getTokenRemainingTime(accessToken) / 1000)
                    .build();

            log.info("=== LOGIN END SUCCESS ===");
            return response;

        } catch (Exception e) {
            log.error("=== LOGIN FAILED ===");
            log.error("Error during login for user {}: {}", request.getLogin(), e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    public LoginResponse refresh(String refreshToken) {
        log.info("=== REFRESH START ===");
        try {
            if (!refreshTokenService.isValidRefreshToken(refreshToken)) {
                throw new RuntimeException("Invalid or expired refresh token");
            }

            String username = refreshTokenService.getUsernameByRefreshToken(refreshToken);
            if (username == null) {
                throw new RuntimeException("Refresh token not found");
            }

            refreshTokenService.deleteRefreshToken(refreshToken);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtService.generateAccessToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            refreshTokenService.saveRefreshToken(newRefreshToken, username);

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getTokenRemainingTime(newAccessToken) / 1000)
                    .build();
        } catch (Exception e) {
            log.error("Refresh error: ", e);
            throw new RuntimeException("Refresh failed: " + e.getMessage(), e);
        } finally {
            log.info("=== REFRESH END ===");
        }
    }

    /**
     * ✅ Выход из системы — добавляем токен в черный список
     */
    public void logout(String accessToken) {
        log.info("=== LOGOUT START ===");
        try {
            if (accessToken != null && !accessToken.isEmpty()) {
                blacklistService.addToBlacklist(accessToken);

                // Извлекаем username для логирования
                try {
                    String username = jwtService.extractUsername(accessToken);
                    log.info("User {} logged out successfully", username);
                } catch (Exception e) {
                    log.info("User logged out (token extracted)");
                }
            }
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
        } finally {
            log.info("=== LOGOUT END ===");
        }
    }

    /**
     * ✅ Блокировка пользователя (для ADMIN)
     */
    public void blockUser(String username) {
        blacklistService.addUserToBlacklist(username);
        log.info("User blocked: {}", username);
    }

    /**
     * ✅ Глобальная блокировка всех токенов
     */
    public void activateGlobalBlacklist() {
        blacklistService.addGlobalBlacklist();
        log.warn("GLOBAL BLACKLIST ACTIVATED by admin");
    }

    /**
     * ✅ Деактивация глобальной блокировки
     */
    public void deactivateGlobalBlacklist() {
        blacklistService.removeGlobalBlacklist();
        log.info("Global blacklist deactivated");
    }
}