package com.example.document_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractRoles(jwt);

        if (authorities.isEmpty()) {
            authorities = defaultGrantedAuthoritiesConverter.convert(jwt);
        }

        log.info("🔐 JWT Authentication: sub={}, authorities={}",
                jwt.getClaimAsString("sub"), authorities);

        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("sub"));
    }

    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        List<Map<String, String>> roles = jwt.getClaim("roles");

        log.debug("🔍 Extracting roles from JWT: {}", roles);

        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(role -> role.get("authority"))
                .filter(authority -> authority != null && !authority.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}