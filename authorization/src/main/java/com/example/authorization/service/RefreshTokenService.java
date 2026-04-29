package com.example.authorization.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;

    private static final String REFRESH_PREFIX = "refresh:";

    // ✅ Сохранить refresh токен (хеш + username)
    public void saveRefreshToken(String refreshToken, String username) {
        String jti = jwtService.extractJti(refreshToken);
        long ttl = jwtService.getTokenRemainingTime(refreshToken);

        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + jti,
                username,
                ttl,
                TimeUnit.MILLISECONDS
        );
        log.info("Refresh token saved for user: {}", username);
    }

    // ✅ Проверить, существует ли refresh токен
    public boolean isValidRefreshToken(String refreshToken) {
        try {
            String jti = jwtService.extractJti(refreshToken);
            String type = jwtService.extractType(refreshToken);

            // Проверяем, что это refresh токен и он существует в Redis
            return "refresh".equals(type) &&
                    Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_PREFIX + jti));
        } catch (Exception e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    // ✅ Получить username по refresh токену
    public String getUsernameByRefreshToken(String refreshToken) {
        String jti = jwtService.extractJti(refreshToken);
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + jti);
    }

    // ✅ Удалить refresh токен (при logout или использовании)
    public void deleteRefreshToken(String refreshToken) {
        String jti = jwtService.extractJti(refreshToken);
        redisTemplate.delete(REFRESH_PREFIX + jti);
        log.info("Refresh token deleted: {}", jti);
    }
}