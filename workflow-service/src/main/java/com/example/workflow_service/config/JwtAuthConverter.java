package com.example.workflow_service.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Стандартные authorities (scope, scp)
        Collection<GrantedAuthority> authorities = new ArrayList<>(
                defaultGrantedAuthoritiesConverter.convert(jwt)
        );

        // ✅ Извлекаем роли из кастомного claim "roles"
        authorities.addAll(extractRoles(jwt));

        JwtAuthenticationToken authToken = new JwtAuthenticationToken(
                jwt,
                authorities,
                getPrincipalClaimName(jwt)
        );

        return authToken;
    }

    /**
     * Извлекает роли из claim "roles"
     * Поддерживает форматы:
     * - ["ROLE_EXECUTOR", "ROLE_MANAGER"]  (массив строк)
     * - [{"authority": "ROLE_EXECUTOR"}]   (массив объектов)
     */
    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        Object rolesObj = jwt.getClaim("roles");
        if (rolesObj == null) {
            return List.of();
        }

        List<String> roles = new ArrayList<>();

        if (rolesObj instanceof List<?> rolesList) {
            for (Object role : rolesList) {
                if (role instanceof String roleStr) {
                    // Формат: ["ROLE_EXECUTOR"]
                    roles.add(roleStr);
                } else if (role instanceof Map<?, ?> roleMap) {
                    // Формат: [{"authority": "ROLE_EXECUTOR"}]
                    Object authority = roleMap.get("authority");
                    if (authority != null) {
                        roles.add(authority.toString());
                    }
                }
            }
        } else if (rolesObj instanceof String roleStr) {
            // Формат: "ROLE_EXECUTOR" (одна строка)
            roles.add(roleStr);
        }

        return roles.stream()
                .map(role -> {
                    // Добавляем префикс ROLE_, если его нет
                    if (!role.startsWith("ROLE_")) {
                        return new SimpleGrantedAuthority("ROLE_" + role);
                    }
                    return new SimpleGrantedAuthority(role);
                })
                .collect(Collectors.toList());
    }

    /**
     * Определяет имя principal (используем userId из JWT)
     */
    private String getPrincipalClaimName(Jwt jwt) {
        // Приоритет: userId (UUID) > sub (login)
        String userId = jwt.getClaim("userId");
        if (userId != null && !userId.isEmpty()) {
            return userId;
        }
        return jwt.getSubject();
    }
}