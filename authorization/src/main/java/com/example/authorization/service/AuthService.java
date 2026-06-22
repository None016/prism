package com.example.authorization.service;

import com.example.authorization.dto.LoginRequest;
import com.example.authorization.dto.LoginResponse;
import com.example.authorization.dto.RegisterRequest;
import com.example.authorization.model.Contractor;
import com.example.authorization.model.Role;
import com.example.authorization.model.Users;
import com.example.authorization.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
    private final EventPublisher eventPublisher;
    private final HttpServletRequest httpServletRequest;
    private final ContractorRepository contractorRepository;
    private final UserContractorRepository userContractorRepository;
    private final UserInstitutionRepository userInstitutionRepository;

    // ✅ МЕТОД REGISTER - ДОЛЖЕН БЫТЬ!
    @Transactional
    public String register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (usersRepository.existsByLogin(request.getLogin())) {
            log.warn("Registration failed: login {} already exists", request.getLogin());
            return "Login already exists!";
        }

        if (usersRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email {} already exists", request.getEmail());
            return "Email already exists!";
        }

        Role userRole = roleRepository.findByNameRole("ROLE_USER")
                .orElseGet(() -> {
                    log.info("Creating default ROLE_USER");
                    Role newRole = new Role();
                    newRole.setNameRole("ROLE_USER");
                    newRole.setNote("Default user role");
                    return roleRepository.save(newRole);
                });

        Contractor contractor = null;
        if (request.getContractorId() != null) {
            contractor = contractorRepository.findById(request.getContractorId())
                    .orElse(null);
        }

        Users user = Users.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .patronymic(request.getPatronymic())
                .phone(request.getPhone())
                .email(request.getEmail())
                .login(request.getLogin())
                .heshPassword(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .birthDate(request.getBirthDate())
                .build();

        Users savedUser = usersRepository.save(user);
        Map<String, Object> metadata = Map.of(
                "ip", httpRequest.getRemoteAddr(),
                "userAgent", httpRequest.getHeader("User-Agent")
        );

        eventPublisher.publishUserRegistered(
                savedUser.getUuid().toString(),
                savedUser.getLogin(),
                savedUser.getEmail(),
                metadata
        );
        log.info("User registered successfully: {}", request.getLogin());

        return "User registered successfully!";
    }

    // ✅ МЕТОД LOGIN
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("=== LOGIN START ===");
        log.info("Login attempt for user: {}", request.getLogin());

        try {
            log.info("Step 1: Authenticating...");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );
            log.info("Step 1: Authentication successful");

            log.info("Step 2: Loading user details...");
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            Users user = usersRepository.findByLogin(request.getLogin())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            log.info("Step 2: User details loaded for: {}", userDetails.getUsername());

            log.info("Step 3: Getting user institutions...");
            List<Integer> institutionIds = userInstitutionRepository.findInstitutionIdsByUserId(user.getUuid());
            log.info("Institutions: {}", institutionIds);

            log.info("Step 4: Getting user contractors...");
            List<Integer> contractorIds = userContractorRepository.findContractorIdsByUserId(user.getUuid());
            log.info("Contractors: {}", contractorIds);

            log.info("Step 5: Generating access token...");
            String accessToken = jwtService.generateAccessToken(userDetails, institutionIds, contractorIds, user.getUuid());
            log.info("Step 5: Access token generated");

            log.info("Step 6: Generating refresh token...");
            String refreshToken = jwtService.generateRefreshToken(userDetails);
            log.info("Step 6: Refresh token generated");

            log.info("Step 7: Saving refresh token to Redis...");
            refreshTokenService.saveRefreshToken(refreshToken, userDetails.getUsername());
            log.info("Step 7: Refresh token saved");

            log.info("Step 8: Building response...");
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getTokenRemainingTime(accessToken) / 1000)
                    .build();

            log.info("=== LOGIN END SUCCESS ===");

            Map<String, Object> metadata = Map.of(
                    "ip", httpRequest.getRemoteAddr(),
                    "userAgent", httpRequest.getHeader("User-Agent")
            );

            eventPublisher.publishUserLoggedIn(
                    user.getUuid().toString(),
                    userDetails.getUsername(),
                    metadata
            );

            return response;

        } catch (Exception e) {
            log.error("=== LOGIN FAILED ===");
            log.error("Error during login for user {}: {}", request.getLogin(), e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    // ✅ ИСПРАВЛЕННЫЙ МЕТОД REFRESH - теперь добавляет contractors и institutions
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

            // ✅ ДОБАВЛЕНО: Загружаем пользователя из БД для получения contractors/institutions
            Users user = usersRepository.findByLogin(username)
                    .orElseThrow(() -> new RuntimeException("User not found during refresh"));

            log.info("Getting user institutions for refresh...");
            List<Integer> institutionIds = userInstitutionRepository.findInstitutionIdsByUserId(user.getUuid());
            log.info("Institutions for refresh: {}", institutionIds);

            log.info("Getting user contractors for refresh...");
            List<Integer> contractorIds = userContractorRepository.findContractorIdsByUserId(user.getUuid());
            log.info("Contractors for refresh: {}", contractorIds);

            // ✅ ИСПРАВЛЕНО: Используем ПОЛНЫЙ метод с institutionIds и contractorIds
            String newAccessToken = jwtService.generateAccessToken(
                    userDetails,
                    institutionIds,
                    contractorIds,
                    user.getUuid()
            );
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            refreshTokenService.saveRefreshToken(newRefreshToken, username);

            log.info("✅ New access token generated with institutions: {}, contractors: {}",
                    institutionIds, contractorIds);

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

    // ✅ МЕТОД LOGOUT
    public void logout(String accessToken) {
        log.info("=== LOGOUT START ===");
        try {
            if (accessToken != null && !accessToken.isEmpty()) {
                blacklistService.addToBlacklist(accessToken);

                try {
                    String username = jwtService.extractUsername(accessToken);
                    eventPublisher.publishUserLoggedOut(username, username);
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

    // ✅ МЕТОДЫ ДЛЯ АДМИНА
    public void blockUser(String username) {
        blacklistService.addUserToBlacklist(username);
        log.info("User blocked: {}", username);
    }

    public void activateGlobalBlacklist() {
        blacklistService.addGlobalBlacklist();
        log.warn("GLOBAL BLACKLIST ACTIVATED by admin");
    }

    public void deactivateGlobalBlacklist() {
        blacklistService.removeGlobalBlacklist();
        log.info("Global blacklist deactivated");
    }
}