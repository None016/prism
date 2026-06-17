// authorization/security/JwtAuthenticationFilter.java
package com.example.authorization.security;

import com.example.authorization.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String path = request.getServletPath();

        log.debug("Processing request to path: {}, Auth header: {}", path,
                authHeader != null ? "present" : "missing");

        // Пропускаем публичные эндпоинты
        if (path.startsWith("/api/auth/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator")) {
            log.debug("Skipping JWT filter for public endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // Проверяем наличие Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        log.debug("Token received: {}...", token.substring(0, Math.min(token.length(), 50)));

        try {
            final String username = jwtService.extractUsername(token);
            log.debug("Extracted username from token: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                log.debug("UserDetails loaded for: {}", username);

                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Successfully authenticated user: {}", username);
                } else {
                    log.warn("Token validation failed for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}