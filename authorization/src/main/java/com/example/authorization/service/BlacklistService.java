package com.example.authorization.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;

    @Value("${blacklist.cache-ttl:900000}")
    private long blacklistTtl;

    private static final String BLACKLIST_PREFIX = "blacklist:jti:";
    private static final String USER_BLACKLIST_PREFIX = "blacklist:user:";
    private static final String GLOBAL_BLACKLIST_KEY = "blacklist:global";

    /**
     * ✅ Добавить токен в черный список (при logout)
     */
    public void addToBlacklist(String accessToken) {
        try {
            String jti = jwtService.extractJti(accessToken);
            long ttl = jwtService.getTokenRemainingTime(accessToken);

            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + jti,
                        "true",
                        ttl,
                        TimeUnit.MILLISECONDS
                );
                log.info("Token {} added to blacklist for {} ms", jti, ttl);
            }
        } catch (Exception e) {
            log.error("Failed to add token to blacklist: {}", e.getMessage());
        }
    }

    /**
     * ✅ Добавить все токены пользователя в черный список
     */
    public void addUserToBlacklist(String username) {
        redisTemplate.opsForValue().set(
                USER_BLACKLIST_PREFIX + username,
                "blocked",
                blacklistTtl,
                TimeUnit.MILLISECONDS
        );
        log.info("User {} added to blacklist", username);
    }

    /**
     * ✅ Глобальный черный список (например, при обнаружении уязвимости)
     */
    public void addGlobalBlacklist() {
        redisTemplate.opsForValue().set(
                GLOBAL_BLACKLIST_KEY,
                "active",
                blacklistTtl,
                TimeUnit.MILLISECONDS
        );
        log.warn("GLOBAL BLACKLIST ACTIVATED!");
    }

    /**
     * ✅ Проверить, не в черном ли списке токен
     */
    public boolean isBlacklisted(String accessToken) {
        try {
            String jti = jwtService.extractJti(accessToken);
            String username = jwtService.extractUsername(accessToken);

            // Проверяем глобальный blacklist
            if (Boolean.TRUE.equals(redisTemplate.hasKey(GLOBAL_BLACKLIST_KEY))) {
                log.warn("Global blacklist is active");
                return true;
            }

            // Проверяем blacklist по jti
            if (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti))) {
                log.info("Token {} is blacklisted", jti);
                return true;
            }

            // Проверяем blacklist по пользователю
            if (Boolean.TRUE.equals(redisTemplate.hasKey(USER_BLACKLIST_PREFIX + username))) {
                log.info("User {} is blacklisted", username);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Failed to check blacklist: {}", e.getMessage());
            return true; // fail secure — при ошибке считаем токен невалидным
        }
    }

    /**
     * ✅ Удалить токен из черного списка (например, при снятии блокировки)
     */
    public void removeFromBlacklist(String accessToken) {
        String jti = jwtService.extractJti(accessToken);
        redisTemplate.delete(BLACKLIST_PREFIX + jti);
        log.info("Token {} removed from blacklist", jti);
    }

    /**
     * ✅ Удалить пользователя из черного списка
     */
    public void removeUserFromBlacklist(String username) {
        redisTemplate.delete(USER_BLACKLIST_PREFIX + username);
        log.info("User {} removed from blacklist", username);
    }

    /**
     * ✅ Деактивировать глобальный черный список
     */
    public void removeGlobalBlacklist() {
        redisTemplate.delete(GLOBAL_BLACKLIST_KEY);
        log.info("Global blacklist deactivated");
    }

    /**
     * ✅ Получить статистику черного списка
     */
    public BlacklistStats getStats() {
        return BlacklistStats.builder()
                .globalActive(redisTemplate.hasKey(GLOBAL_BLACKLIST_KEY))
                .build();
    }

    @lombok.Builder
    @lombok.Data
    public static class BlacklistStats {
        private boolean globalActive;
    }
}