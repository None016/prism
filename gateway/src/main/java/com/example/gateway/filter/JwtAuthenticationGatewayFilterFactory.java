package com.example.gateway.filter;

import com.example.gateway.utils.KeyUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
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

@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationGatewayFilterFactory.class);
    private final KeyUtils keyUtils;

    public JwtAuthenticationGatewayFilterFactory(KeyUtils keyUtils) {
        super(Object.class);
        this.keyUtils = keyUtils;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Пропускаем эндпоинты аутентификации
            if (path.startsWith("/api/auth/") ||
                    path.startsWith("/swagger-ui") ||
                    path.startsWith("/v3/api-docs") ||
                    path.equals("/actuator/health")) {
                return chain.filter(exchange);
            }

            // Проверяем заголовок Authorization
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

                log.debug("Token validated for user: {}, path: {}", username, path);

                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", username)
                        .header("X-User-Roles", roles != null ? roles.toString() : "")
                        .header("X-Authenticated", "true")
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                log.error("JWT validation failed for path {}: {}", path, e.getMessage());
                return unauthorized(exchange, "Invalid or expired token: " + e.getMessage());
            }
        };
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
}