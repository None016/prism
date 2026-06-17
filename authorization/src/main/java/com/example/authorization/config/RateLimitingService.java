// AuthService/config/RateLimitingConfig.java
package com.example.authorization.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RateLimitingService {

    private final RedisTemplate<String, String> redisTemplate;

    public RateLimitingService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key, int maxAttempts, int windowSeconds) {
        String redisKey = "ratelimit:" + key;
        Long attempts = redisTemplate.opsForValue().increment(redisKey);

        if (attempts == 1) {
            redisTemplate.expire(redisKey, java.time.Duration.ofSeconds(windowSeconds));
        }

        return attempts <= maxAttempts;
    }

    public void reset(String key) {
        redisTemplate.delete("ratelimit:" + key);
    }
}