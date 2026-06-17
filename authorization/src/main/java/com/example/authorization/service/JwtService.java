package com.example.authorization.service;

import com.example.authorization.utils.KeyUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final KeyUtils keyUtils;

    @Value("${jwt.access-token-expiration:900000}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:2592000000}")
    private Long refreshTokenExpiration;

// В методе generateAccessToken добавьте institutionIds и contractorIds

    public String generateAccessToken(UserDetails userDetails, List<Integer> institutionIds, List<Integer> contractorIds, UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        claims.put("type", "access");
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("institutions", institutionIds != null ? institutionIds : List.of());
        claims.put("contractors", contractorIds != null ? contractorIds : List.of());
        claims.put("userId", userId.toString());  // Добавляем userId в токен!

        return generateToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    // Старый метод оставьте для обратной совместимости
// В JwtService.java - оставьте для совместимости
    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(userDetails, List.of(), List.of(), UUID.randomUUID());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("jti", UUID.randomUUID().toString());

        return generateToken(claims, userDetails.getUsername(), refreshTokenExpiration);
    }

    private String generateToken(Map<String, Object> claims, String subject, Long expiration) {
        PrivateKey privateKey = keyUtils.getPrivateKey();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token) {
        return extractClaim(token, claims -> claims.get("jti", String.class));
    }

    public String extractType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        PublicKey publicKey = keyUtils.getPublicKey();

        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Long getTokenRemainingTime(String token) {
        return extractExpiration(token).getTime() - System.currentTimeMillis();
    }
}