package com.example.authorization.controller;

import com.example.authorization.utils.KeyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
public class JwksController {

    private final KeyUtils keyUtils;

    @GetMapping("/jwks.json")
    public Map<String, Object> getJwks() {
        // Преобразуем публичный ключ в JWKS формат
        byte[] encoded = keyUtils.getPublicKey().getEncoded();
        String modulus = Base64.getUrlEncoder().withoutPadding().encodeToString(
                java.util.Arrays.copyOfRange(encoded, 0, 256) // упрощённо
        );
        String exponent = Base64.getUrlEncoder().withoutPadding().encodeToString(
                java.util.Arrays.copyOfRange(encoded, 256, 260)
        );

        return Map.of(
                "keys", java.util.List.of(
                        Map.of(
                                "kty", "RSA",
                                "alg", "RS256",
                                "use", "sig",
                                "n", modulus,
                                "e", exponent,
                                "kid", "auth-service-key"
                        )
                )
        );
    }
}
