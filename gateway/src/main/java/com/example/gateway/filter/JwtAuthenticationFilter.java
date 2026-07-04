package com.example.gateway.filter;

import com.example.gateway.utils.KeyUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter implements GatewayFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final KeyUtils keyUtils;

    public JwtAuthenticationFilter(KeyUtils keyUtils) {
        this.keyUtils = keyUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Пропускаем публичные endpoints
        if (path.startsWith("/api/auth/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/actuator/health")) {
            return chain.filter(exchange);
        }

        List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null || authHeaders.isEmpty()) {
            log.warn("Missing Authorization header for path: {}", path);
            return unauthorized(exchange, "Missing Authorization header");
        }

        String authHeader = authHeaders.get(0);
        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Invalid Authorization header format for path: {}", path);
            return unauthorized(exchange, "Invalid Authorization header format");
        }

        String token = authHeader.substring(7);

        try {
            PublicKey publicKey = keyUtils.getPublicKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            Object roles = claims.get("roles");

            // ✅ ИЗВЛЕКАЕМ contractors и institutions из JWT
            Object contractors = claims.get("contractors");
            Object institutions = claims.get("institutions");

            // Формируем строковые представления для заголовков
            String contractorsHeader = extractIdsAsString(contractors);
            String institutionsHeader = extractIdsAsString(institutions);
            String rolesHeader = roles != null ? roles.toString() : "";

            log.debug("Token validated - user: {}, roles: {}, contractors: {}, institutions: {}, path: {}",
                    username, rolesHeader, contractorsHeader, institutionsHeader, path);

            // ✅ ФОРМИРУЕМ ЗАГОЛОВКИ
            ServerHttpRequest.Builder requestBuilder = request.mutate()
                    .header("X-User-Id", username)
                    .header("X-User-Roles", rolesHeader)
                    .header("X-Authenticated", "true");

            // ✅ ДОБАВЛЯЕМ contractors если есть
            if (!contractorsHeader.isEmpty()) {
                requestBuilder.header("X-User-Contractors", contractorsHeader);
            }

            // ✅ ДОБАВЛЯЕМ institutions если есть
            if (!institutionsHeader.isEmpty()) {
                requestBuilder.header("X-User-Institutions", institutionsHeader);
            }

            ServerHttpRequest mutatedRequest = requestBuilder.build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("JWT validation failed for path {}: {}", path, e.getMessage());
            return unauthorized(exchange, "Invalid or expired token: " + e.getMessage());
        }
    }

    /**
     * ✅ Универсальный метод извлечения ID из JWT claims
     * Поддерживает:
     * - List<Integer> → "1,2,3"
     * - List<String> → "1,2,3"
     * - String "1,2,3" → "1,2,3"
     * - Integer → "1"
     * - null → ""
     */
    private String extractIdsAsString(Object value) {
        if (value == null) {
            return "";
        }

        // Если это список (самый частый случай)
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return "";
            }
            return list.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        }

        // Если это строка
        if (value instanceof String) {
            return (String) value;
        }

        // Если это число
        return value.toString();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"error\": \"%s\", \"timestamp\": \"%s\"}",
                message, java.time.Instant.now());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}